package me.maagk.johannes.virtualpeer.chat

import me.maagk.johannes.virtualpeer.survey.question.EmojiQuestion

class EmojiQuestionMessage(val emojiQuestion: EmojiQuestion) : QuestionMessage(EMOJI_QUESTION, emojiQuestion.question, emojiQuestion)