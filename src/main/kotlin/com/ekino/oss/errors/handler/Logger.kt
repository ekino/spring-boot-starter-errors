package com.ekino.oss.errors.handler

import org.slf4j.LoggerFactory

fun <T : Any> T.logger() = lazy { LoggerFactory.getLogger(this.javaClass) }
