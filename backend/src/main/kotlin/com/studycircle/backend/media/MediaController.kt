package com.studycircle.backend.media

import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.util.UUID

data class UploadUrlRequest(val mediaType: String, val extension: String)
data class UploadUrlResponse(val uploadUrl: String, val remoteMediaUrl: String)

// Local-disk implementation for dev/MVP. The point of the pre-signed-URL
// *shape* from step 2 is that the client never needs to know which storage
// backend sits behind it — it just POSTs for a URL, PUTs bytes to that URL,
// and stores the returned remoteMediaUrl. Swapping this for real S3/GCS
// pre-signed URLs later only touches this one controller; nothing in the
// shared module's MediaApi or SubmissionRepositoryImpl would change.
@RestController
@RequestMapping("/api/v1/media")
class MediaController {
    private val storageDir = File(System.getProperty("java.io.tmpdir"), "study-circle-media").apply { mkdirs() }

    @PostMapping("/upload-url")
    fun getUploadUrl(@RequestBody request: UploadUrlRequest): ResponseEntity<UploadUrlResponse> {
        val fileId = "${UUID.randomUUID()}.${request.extension}"
        return ResponseEntity.ok(
            UploadUrlResponse(
                uploadUrl = "/api/v1/media/upload/$fileId",
                remoteMediaUrl = "/api/v1/media/$fileId"
            )
        )
    }

    @PutMapping("/upload/{fileId}", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun upload(@PathVariable fileId: String, @RequestBody bytes: ByteArray): ResponseEntity<Void> {
        File(storageDir, fileId).writeBytes(bytes)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{fileId}")
    fun download(@PathVariable fileId: String): ResponseEntity<FileSystemResource> {
        val file = File(storageDir, fileId)
        if (!file.exists()) return ResponseEntity.notFound().build()
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(FileSystemResource(file))
    }
}
