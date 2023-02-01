package com.goforer.advancedapparchitecture.data

import javax.inject.Singleton

@Singleton
class Params constructor(val replyCount: Int, val query: Query)