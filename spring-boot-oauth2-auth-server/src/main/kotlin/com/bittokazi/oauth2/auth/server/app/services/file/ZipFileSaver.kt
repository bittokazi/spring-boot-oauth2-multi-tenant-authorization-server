package com.bittokazi.oauth2.auth.server.app.services.file

import com.bittokazi.oauth2.auth.server.app.models.base.FileInput
import com.bittokazi.oauth2.auth.server.app.models.base.UploadObject
import org.apache.tomcat.util.http.fileupload.FileUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class ZipFileSaver: FileService {

    override fun save(fileInput: FileInput): UploadObject {
        val file: MultipartFile = fileInput.file
        val folder: String = fileInput.folder

        val fileName = file.originalFilename
        val pattern = "([A-Z,a-z,.,0-9,_,-])"
        val r: Pattern = Pattern.compile(pattern)
        val m: Matcher = r.matcher(fileName)
        var newFileName = ""
        while (m.find()) {
            newFileName += m.group(1)
        }
        val absoluteFilePath = folder + File.separator

        //newFileName = Utils.randomNumberGenerator(30) + "-" + newFileName
        newFileName = fileInput.fileName

        val bytes: ByteArray
        try {
            bytes = file.bytes
            if (!Files.exists(Paths.get(absoluteFilePath))) {
                Files.createDirectories(Paths.get(absoluteFilePath))
            }
            val fileZip = absoluteFilePath + newFileName + ".zip"

            if (Files.exists(Paths.get(absoluteFilePath + newFileName))) {
                FileUtils.deleteDirectory(File(absoluteFilePath + newFileName));
            }

            if (Files.exists(Paths.get(absoluteFilePath + newFileName + ".zip"))) {
                File(fileZip).delete()
            }
            val path: Path = Paths.get(absoluteFilePath + newFileName + ".zip")
            Files.write(path, bytes)


            val destDir: File = File(absoluteFilePath + newFileName)
            val buffer = ByteArray(1024)
            val zis = ZipInputStream(FileInputStream(fileZip))
            var zipEntry = zis.nextEntry
            while (zipEntry != null) {
                val newFile: File = newFile(destDir, zipEntry)
                if (zipEntry.isDirectory) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw IOException("Failed to create directory $newFile")
                    }
                } else {
                    // fix for Windows-created archives
                    val parent: File = newFile.getParentFile()
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw IOException("Failed to create directory $parent")
                    }

                    // write file content
                    val fos: FileOutputStream = FileOutputStream(newFile)
                    var len: Int
                    while ((zis.read(buffer).also { len = it }) > 0) {
                        fos.write(buffer, 0, len)
                    }
                    fos.close()
                }
                zipEntry = zis.nextEntry
            }
            zis.closeEntry()
            zis.close()
            File(fileZip).delete()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return UploadObject(
            fileInput.fileName,
            ""
        )
    }

    @Throws(IOException::class)
    fun newFile(destinationDir: File, zipEntry: ZipEntry): File {
        val destFile = File(destinationDir, zipEntry.name)

        val destDirPath = destinationDir.canonicalPath
        val destFilePath = destFile.canonicalPath

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw IOException("Entry is outside of the target dir: " + zipEntry.name)
        }

        return destFile
    }
}
