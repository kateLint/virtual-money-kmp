package com.keren.virtualmoney

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform