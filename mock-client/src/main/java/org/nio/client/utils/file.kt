package org.nio.client.utils

import com.opencsv.CSVReader
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import java.io.InputStreamReader


fun readCsvFile(filePart: MultipartFile): Flux<Array<String>> {
    return InputStreamReader(filePart.inputStream)
        .use { reader ->
            CSVReader(reader).use { csvReader ->
                Flux.fromIterable(csvReader.readAll())
            }
        }
}
//file.block().content().map {t-> CSVReader(InputStreamReader(t.asInputStream())).readNext() }.blockLast()