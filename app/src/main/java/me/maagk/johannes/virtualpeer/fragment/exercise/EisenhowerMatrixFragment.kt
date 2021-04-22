package me.maagk.johannes.virtualpeer.fragment.exercise

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.exercise.AddGoalDialog
import me.maagk.johannes.virtualpeer.exercise.EisenhowerMatrix
import me.maagk.johannes.virtualpeer.goals.Goal
import me.maagk.johannes.virtualpeer.goals.GoalStorage
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

class EisenhowerMatrixFragment : Fragment(R.layout.fragment_eisenhower_matrix), AddGoalDialog.OnGoalCompletedListener, OnGoalOptionsClickedListener {

    companion object {
        const val TAG = "eisenhowerMatrix"
    }

    private lateinit var storage: GoalStorage

    private lateinit var urgentImportantPart: MatrixPart
    private lateinit var notUrgentImportantPart: MatrixPart
    private lateinit var urgentNotImportantPart: MatrixPart
    private lateinit var notUrgentNotImportantPart: MatrixPart

    inner class MatrixPart(val rootLayout: LinearLayout, val position: EisenhowerMatrix.Position) {

        val goals = ArrayList<Goal>()

        val titleText: TextView = rootLayout.findViewById(R.id.eisenhowerMatrixPositionTitle)
        val goalListCard: CardView = rootLayout.findViewById(R.id.goalListCard)
        val goalList: RecyclerView = goalListCard.findViewById(R.id.goalList)

        private val itemTouchHelper: ItemTouchHelper

        val goalListAdapter: GoalListAdapter

        init {
            // setting the background color of this part
            rootLayout.setBackgroundColor(position.getColor(requireContext()))

            // adjusting the position of this part's list
            // 2 of these have the majority of their padding on the left, the others on the right
            val marginLarge = Utils.dpToPx(35f, requireContext().resources.displayMetrics).toInt()
            val marginSmall = Utils.dpToPx(2.5f, requireContext().resources.displayMetrics).toInt()

            val layoutParams = goalListCard.layoutParams as LinearLayout.LayoutParams

            when(position) {
                EisenhowerMatrix.Position.URGENT_IMPORTANT, EisenhowerMatrix.Position.URGENT_NOT_IMPORTANT -> {
                    layoutParams.marginStart = marginLarge
                    layoutParams.marginEnd = marginSmall
                }

                EisenhowerMatrix.Position.NOT_URGENT_IMPORTANT, EisenhowerMatrix.Position.NOT_URGENT_NOT_IMPORTANT -> {
                    layoutParams.marginStart = marginSmall
                    layoutParams.marginEnd = marginLarge
                }
            }

            goalListCard.layoutParams = layoutParams

            titleText.text = position.getTitle(requireContext())

            val layoutManager = LinearLayoutManager(context)
            goalList.layoutManager = layoutManager

            goalListAdapter = GoalListAdapter()
            goalList.adapter = goalListAdapter

            // creating an instance of the SwipeController class; this is responsible for handling sliding actions on each card
            val swipeController = SwipeCallback()
            swipeController.onGoalOptionsClickedListener = this@EisenhowerMatrixFragment

            itemTouchHelper = ItemTouchHelper(swipeController)
            itemTouchHelper.attachToRecyclerView(goalList)
        }

        fun detachItemTouchHelper() {
            itemTouchHelper.attachToRecyclerView(null)
        }

        inner class GoalListAdapter() : RecyclerView.Adapter<GoalViewHolder>() {

            private lateinit var layoutInflater: LayoutInflater

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
                if(!::layoutInflater.isInitialized)
                    layoutInflater = LayoutInflater.from(parent.context)

                val view = layoutInflater.inflate(R.layout.view_eisenhower_matrix_goal, parent, false)
                return GoalViewHolder(view)
            }

            override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
                holder.bind(goals[position])
            }

            override fun getItemCount(): Int {
                return goals.size
            }

        }

        inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val goalCard: CardView = itemView.findViewById(R.id.goalCard)
            val goalName: TextView = goalCard.findViewById(R.id.goalName)
            val goalInfo: TextView = goalCard.findViewById(R.id.goalInfo)
            val goalCheckBox: CheckBox = goalCard.findViewById(R.id.goalCheckBox)

            var initialRight = Int.MIN_VALUE
            var initialX = Float.MIN_VALUE

            private lateinit var goal: Goal

            init {
                // TODO: should this be here?
                goalCard.setOnClickListener {
                    goalCheckBox.isChecked = !goalCheckBox.isChecked
                }

                goalCheckBox.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
                    goal.completed = checked
                }
            }

            fun bind(goal: Goal) {
                this.goal = goal

                goalName.text = goal.name
                goalInfo.text = if(goal.deadline == null) {
                    getString(R.string.eisenhower_matrix_goal_info_no_deadline)
                } else {
                    val dateFormat = DateFormat.getDateFormat(requireContext())
                    val oldDate = Date.from(goal.deadline.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    val formattedDate = dateFormat.format(oldDate)
                    getString(R.string.eisenhower_matrix_goal_info, formattedDate)
                }

                goalCheckBox.isChecked = goal.completed
            }
        }

        inner class SwipeCallback : ItemTouchHelper.Callback() {

            private var swipeBack = false
            private var buttonsVisible = false

            private val singleButtonWidth = Utils.dpToPx(18f, requireContext().resources.displayMetrics)
            private val horizontalPadding = Utils.dpToPx(2.5f, requireContext().resources.displayMetrics)
            private val buttonHorizontalPadding = Utils.dpToPx(2f, requireContext().resources.displayMetrics)

            //                             two buttons               total padding             padding on each button
            private val totalButtonWidth = (singleButtonWidth * 2) + (horizontalPadding * 2) + (buttonHorizontalPadding * 2 * 2)

            private lateinit var buttons: RectF

            lateinit var onGoalOptionsClickedListener: OnGoalOptionsClickedListener

            private val cornerRadius = Utils.dpToPx(15f, requireContext().resources.displayMetrics)

            private val paint = Paint()
            private val maskPaint = Paint()
            private lateinit var maskBitmap: Bitmap

            private val deleteButtonVectorDrawable = VectorDrawableCompat.create(requireContext().resources, R.drawable.ic_delete, requireContext().theme)
            private val pinButtonVectorDrawable = VectorDrawableCompat.create(requireContext().resources, R.drawable.ic_pin, requireContext().theme)

            init {
                paint.isAntiAlias = true

                maskPaint.isAntiAlias = true
                maskPaint.isFilterBitmap = true
                maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

                deleteButtonVectorDrawable?.setBounds(0, 0, singleButtonWidth.toInt(), singleButtonWidth.toInt())
                pinButtonVectorDrawable?.setBounds(0, 0, singleButtonWidth.toInt(), singleButtonWidth.toInt())
            }

            @SuppressLint("ClickableViewAccessibility")
            private fun setOnTouchListener(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                recyclerView.setOnTouchListener start@ { view, motionEvent ->
                    if(motionEvent == null)
                        return@start false

                    buttonsVisible = -dX >= totalButtonWidth
                    swipeBack = motionEvent.action == MotionEvent.ACTION_CANCEL || motionEvent.action == MotionEvent.ACTION_UP

                    if(buttonsVisible)
                        setOnTouchDownListener(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                    if(motionEvent.action == MotionEvent.ACTION_UP)
                        view.performClick()

                    false
                }
            }

            @SuppressLint("ClickableViewAccessibility")
            private fun setOnTouchDownListener(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                recyclerView.setOnTouchListener { _, motionEvent ->
                    if(motionEvent.action == MotionEvent.ACTION_DOWN)
                        setOnTouchUpListener(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                    false
                }
            }

            @SuppressLint("ClickableViewAccessibility")
            private fun setOnTouchUpListener(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                recyclerView.setOnTouchListener { _, motionEvent ->
                    if(motionEvent.action == MotionEvent.ACTION_UP) {
                        // closing the menu
                        super.onChildDraw(canvas, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive)
                        swipeBack = true
                        recyclerView.setOnTouchListener { _, _ -> false }
                        buttonsVisible = false

                        /*
                         * Creating relative values that go from the top-left corner to the bottom-right corner of the options menu.
                         * 0, 0 = top-left corner
                         * width, height = bottom-right corner
                         */
                        val relativeX = (motionEvent.x - viewHolder.itemView.left - buttons.left) / buttons.width()
                        val relativeY = (motionEvent.y - viewHolder.itemView.top) / buttons.height()

                        // checking whether the user's click is inside the menu
                        val inButtonsHorizontally = relativeX in 0.0..1.0
                        val inButtonsVertically = relativeY in 0.0..1.0
                        val inButtons = inButtonsHorizontally && inButtonsVertically

                        if(::onGoalOptionsClickedListener.isInitialized && inButtons && viewHolder is GoalViewHolder) {
                            // calling the appropriate method on the listener depending on which half of the menu was clicked
                            if(relativeX >= 0.5)
                                onGoalOptionsClickedListener.onPinClicked(this@MatrixPart, viewHolder)
                            else
                                onGoalOptionsClickedListener.onDeleteClicked(this@MatrixPart, viewHolder)
                        }
                    }

                    false
                }
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 1f / 100f
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return defaultValue / 1000
            }

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return makeMovementFlags(0, ItemTouchHelper.START)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
                if(swipeBack) {
                    swipeBack = false
                    return 0
                }
                return super.convertToAbsoluteDirection(flags, layoutDirection)
            }

            override fun onChildDrawOver(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if(viewHolder == null || viewHolder !is GoalViewHolder)
                    return

                // TODO: the user shouldn't be able to open two menus at the same time
                // TODO: scrolling should close all menus

                // this will freeze the view's x position
                if(viewHolder.initialX == Float.MIN_VALUE)
                    viewHolder.initialX = viewHolder.itemView.x

                viewHolder.itemView.x = viewHolder.initialX

                if(viewHolder.initialRight == Int.MIN_VALUE)
                    viewHolder.initialRight = viewHolder.itemView.right

                // these values don't include margins
                val width = viewHolder.itemView.width
                val height = viewHolder.itemView.height

                // creating a bitmap the size of the view and a canvas to work with it
                val offscreenBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val offscreenCanvas = Canvas(offscreenBitmap)

                // drawing the buttons to the offscreen canvas (see method for proper documentation)
                drawButtons(offscreenCanvas, cornerRadius, dX)

                // creating a mask if it doesn't already exist; this is used to round the corners of the buttons drawn (to keep them within the card)
                if(!::maskBitmap.isInitialized)
                    maskBitmap = createMask(width, height)

                // drawing the mask over the buttons to cut everything off that's outside the desired region
                offscreenCanvas.drawBitmap(maskBitmap, 0f, 0f, maskPaint)

                // drawing the result of all the offscreen drawing to the actually visible canvas
                canvas.drawBitmap(offscreenBitmap, viewHolder.itemView.left.toFloat(), viewHolder.itemView.top.toFloat(), paint)

                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE && !buttonsVisible) {
                    setOnTouchListener(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            private fun createMask(width: Int, height: Int): Bitmap {
                val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
                val canvas = Canvas(mask)

                val paint = Paint()
                paint.isAntiAlias = true
                paint.color = Color.WHITE

                val widthFloat = width.toFloat()
                val heightFloat = height.toFloat()

                canvas.drawRect(0f, 0f, widthFloat, heightFloat, paint)

                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

                canvas.drawRoundRect(0f, 0f, widthFloat, heightFloat, cornerRadius, cornerRadius, paint)

                return mask
            }

            private fun drawButtons(canvas: Canvas, cornerRadius: Float, dX: Float) {
                if(deleteButtonVectorDrawable == null || pinButtonVectorDrawable == null)
                    return

                // setting up the paint (anti-aliasing should be done)
                val paint = Paint()
                paint.isAntiAlias = true
                paint.color = Utils.getColor(requireContext(), R.color.colorBackgroundCardGoalMenu)

                // the menu's width follows the amount of swiping done up to a specified max (the menu's total width)
                val buttonWidth = min(-dX, totalButtonWidth)
                buttons = RectF(canvas.width - buttonWidth, 0f, canvas.width.toFloat(), canvas.height.toFloat())

                // drawing the menu background
                canvas.drawRoundRect(buttons, cornerRadius, cornerRadius, paint)

                // preparing a bitmap the size of the delete button's drawable and a canvas to draw on it
                val deleteButtonBitmap = Bitmap.createBitmap(deleteButtonVectorDrawable.bounds.width(), deleteButtonVectorDrawable.bounds.height(), Bitmap.Config.ARGB_8888)
                val deleteButtonCanvas = Canvas(deleteButtonBitmap)

                /*
                 * Calculating the point at which the icon will be cut off (this should exactly follow the left border of the menu itself).
                 * dX is the exact distance from the end of the card. This is now calculating the distance from the end to be able to take
                 * dX into account in order to move the border.
                 *
                 * Since the swipe starts on the right, dX is a negative value that goes more into the negatives the further the swipe goes.
                 * This first adds the total width of the menu to this value which is a bit too much because it doesn't take padding into account.
                 * This padding is then subtracted again. This includes both the general horizontal padding for the entire menu
                 * as well as a single button's horizontal padding).
                 */
                val deleteButtonLeftClip = max(0f, dX + totalButtonWidth - horizontalPadding - buttonHorizontalPadding)

                // clipping the part of the canvas where the icon should not be drawn
                deleteButtonCanvas.clipRect(deleteButtonLeftClip, 0f, deleteButtonCanvas.width.toFloat(), deleteButtonCanvas.height.toFloat())

                // drawing the delete icon onto its canvas
                deleteButtonVectorDrawable.draw(deleteButtonCanvas)

                // preparing the delete icon's coordinates and drawing the bitmap (with the icon on it) onto the main canvas
                val deleteButtonLeft = buttons.right - totalButtonWidth + horizontalPadding + buttonHorizontalPadding
                val deleteButtonTop = buttons.centerY() - (deleteButtonBitmap.height / 2)
                canvas.drawBitmap(deleteButtonBitmap, deleteButtonLeft, deleteButtonTop, paint)


                // preparing a bitmap the size of the pin button's drawable and a canvas to draw on it
                val pinButtonBitmap = Bitmap.createBitmap(pinButtonVectorDrawable.bounds.width(), pinButtonVectorDrawable.bounds.height(), Bitmap.Config.ARGB_8888)
                val pinButtonCanvas = Canvas(pinButtonBitmap)

                // to see what this does, look at the large comment above the same thing for the delete icon
                val pinButtonLeftClip = max(0f, dX + horizontalPadding + buttonHorizontalPadding + singleButtonWidth)
                pinButtonCanvas.clipRect(pinButtonLeftClip, 0f, pinButtonCanvas.width.toFloat(), pinButtonCanvas.height.toFloat())

                // drawing the pin icon onto its canvas
                pinButtonVectorDrawable.draw(pinButtonCanvas)

                // preparing the pin icon's coordinates and drawing the bitmap (with the icon on it) onto the main canvas
                val pinButtonLeft = buttons.right - horizontalPadding - buttonHorizontalPadding - singleButtonWidth
                val pinButtonTop = buttons.centerY() - (pinButtonBitmap.height / 2)
                canvas.drawBitmap(pinButtonBitmap, pinButtonLeft, pinButtonTop, paint)
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storage = GoalStorage(requireContext())

        urgentImportantPart = MatrixPart(view.findViewById(R.id.urgentImportantLayout), EisenhowerMatrix.Position.URGENT_IMPORTANT)
        notUrgentImportantPart = MatrixPart(view.findViewById(R.id.notUrgentImportantLayout), EisenhowerMatrix.Position.NOT_URGENT_IMPORTANT)
        urgentNotImportantPart = MatrixPart(view.findViewById(R.id.urgentNotImportantLayout), EisenhowerMatrix.Position.URGENT_NOT_IMPORTANT)
        notUrgentNotImportantPart = MatrixPart(view.findViewById(R.id.notUrgentNotImportantLayout), EisenhowerMatrix.Position.NOT_URGENT_NOT_IMPORTANT)

        for(goal in storage.goals) {
            when(goal.position) {
                EisenhowerMatrix.Position.URGENT_IMPORTANT -> urgentImportantPart.goals.add(goal)
                EisenhowerMatrix.Position.NOT_URGENT_IMPORTANT -> notUrgentImportantPart.goals.add(goal)
                EisenhowerMatrix.Position.URGENT_NOT_IMPORTANT -> urgentNotImportantPart.goals.add(goal)
                EisenhowerMatrix.Position.NOT_URGENT_NOT_IMPORTANT -> notUrgentNotImportantPart.goals.add(goal)
            }
        }

        val addGoalButton: FloatingActionButton = view.findViewById(R.id.addGoal)
        addGoalButton.setOnClickListener {
            val dialog = AddGoalDialog(requireContext())
            dialog.onGoalCompletedListener = this
            dialog.show()
        }
    }

    override fun onGoalCompleted(goal: Goal) {
        val part = when(goal.position) {
            EisenhowerMatrix.Position.URGENT_IMPORTANT -> urgentImportantPart
            EisenhowerMatrix.Position.NOT_URGENT_IMPORTANT -> notUrgentImportantPart
            EisenhowerMatrix.Position.URGENT_NOT_IMPORTANT -> urgentNotImportantPart
            EisenhowerMatrix.Position.NOT_URGENT_NOT_IMPORTANT -> notUrgentNotImportantPart
        }

        part.goals.add(goal)

        val adapter = part.goalList.adapter
        adapter?.let {
            adapter.notifyItemInserted(adapter.itemCount)
        }

        storage.goals.add(goal)
        storage.save()
    }

    override fun onDetach() {
        super.onDetach()

        urgentImportantPart.detachItemTouchHelper()
        notUrgentImportantPart.detachItemTouchHelper()
        urgentNotImportantPart.detachItemTouchHelper()
        notUrgentNotImportantPart.detachItemTouchHelper()
    }

    override fun onPause() {
        super.onPause()

        storage.save()
    }

    override fun onDeleteClicked(matrixPart: MatrixPart, viewHolder: MatrixPart.GoalViewHolder) {
        val goal = matrixPart.goals[viewHolder.adapterPosition]

        matrixPart.goalListAdapter.notifyItemRemoved(viewHolder.adapterPosition)
        matrixPart.goals.remove(goal)

        storage.deleteGoal(goal)
        storage.save()
    }

    override fun onPinClicked(matrixPart: MatrixPart, viewHolder: MatrixPart.GoalViewHolder) {
        val goal = matrixPart.goals[viewHolder.adapterPosition]
        goal.pinned = !goal.pinned
    }

}