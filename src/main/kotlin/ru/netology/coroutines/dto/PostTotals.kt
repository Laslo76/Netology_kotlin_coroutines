package ru.netology.coroutines.dto

data class PostTotals(
    val post: Post,
    val comments: List<CommentTotal>,
    val author: Author,
)
