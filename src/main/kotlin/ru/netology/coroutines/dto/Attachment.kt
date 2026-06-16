package ru.netology.coroutines.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val description: String,
    val type: AttachmentType,
)