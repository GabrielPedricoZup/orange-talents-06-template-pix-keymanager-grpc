package com.zupedu.gabrielpedrico

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("com.zupedu.gabrielpedrico")
		.start()
}

