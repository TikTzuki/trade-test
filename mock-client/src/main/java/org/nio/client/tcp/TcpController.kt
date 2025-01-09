package org.nio.client.tcp

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*

@RestController("/api/v1/tcp")
class TcpController @Autowired constructor(
    val workerService: TcpWorkerService
) {
    @PostMapping("/bulk-transfers", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun bulkTransfers(
        @RequestParam parallel: Int,
        @RequestParam userCount: Long,
        @RequestParam transactionPerUser: Int,
        @RequestPart("file")
        fileFlux: FilePart
    ): String {
        val accountIds: List<String> = DataBufferUtils.join(fileFlux.content())
            .map { buffer ->
                buffer.asInputStream(true)
                    .bufferedReader().use { reader ->
                        reader.readLines()
                    }
            }.block()!!
        workerService.start(accountIds, userCount, transactionPerUser)
        return "Hello, World!"
    }

    @GetMapping("/stop-transfers")
    fun stopScheduler(): String {
        workerService.shutdown()
        return "Stopped"
    }
}