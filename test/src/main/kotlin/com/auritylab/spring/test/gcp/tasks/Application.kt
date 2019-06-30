package com.auritylab.spring.test.gcp.tasks

import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@ServletComponentScan
@SpringBootApplication
@EnableCloudTasks
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
