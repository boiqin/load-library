package com.boiqin.load

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileUtils {
    /**
     * zip文件压缩
     * @param inputFile 待压缩文件夹/文件名
     * @param outputFile 生成的压缩包名字
     */
    @Throws(Exception::class)
    fun zipCompress(inputFile: String, outputFile: String) { //创建zip输出流
        val out =
            ZipOutputStream(FileOutputStream(outputFile))
        //创建缓冲输出流
        val bos = BufferedOutputStream(out)
        val input = File(inputFile)
        compress(out, bos, input, null)
        bos.close()
        out.close()
    }

    /**
     * @param name 压缩文件名，可以写为null保持默认
     */
//递归压缩
    @Throws(IOException::class)
    fun compress(
        out: ZipOutputStream,
        bos: BufferedOutputStream,
        input: File,
        _name: String?
    ) {
        val name = _name ?: input.name

        //如果路径为目录（文件夹）
        if (input.isDirectory) { //取出文件夹中的文件（或子文件夹）
            val fileList = input.listFiles()
            if (fileList.isEmpty()) //如果文件夹为空，则只需在目的地zip文件中写入一个目录进入
            {
                out.putNextEntry(ZipEntry("$name/"))
            } else  //如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩
            {
                for (i in fileList.indices) {
                    compress(
                        out,
                        bos,
                        fileList[i],
                        name + "/" + fileList[i].name
                    )
                }
            }
        } else  //如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中
        {
            out.putNextEntry(ZipEntry(name))
            val fos = FileInputStream(input)
            val bis = BufferedInputStream(fos)
            var len: Int
            //将源文件写入到zip文件中
            val buf = ByteArray(1024)
            while (bis.read(buf).also { len = it } != -1) {
                bos.write(buf, 0, len)
                // 当一个文件夹中有多个文件时，压缩后的文件数据将会缺失。需要刷新bos流
                bos.flush()
            }
            bis.close()
            fos.close()
        }
    }

    /**
     * zip解压
     * @param inputFile 待解压文件名
     * @param destDirPath  解压路径
     */
    @Throws(Exception::class)
    fun zipUncompress(inputFile: String, destDirPath: String): Boolean {
        var success = false
        val srcFile = File(inputFile) //获取当前压缩文件
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw Exception(srcFile.path + "not exists")
        }
        //开始解压
        //构建解压输入流
        val zIn = ZipInputStream(FileInputStream(srcFile))
        var entry: ZipEntry? = null
        var file: File? = null
        while (zIn.nextEntry.also { entry = it } != null) {
            if (!entry!!.isDirectory) {
                file = File(destDirPath, entry!!.name)
                if (!file.exists()) {
                    File(file.parent).mkdirs() //创建此文件的上级目录
                }
                val out: OutputStream = FileOutputStream(file)
                val bos = BufferedOutputStream(out)
                var len: Int
                val buf = ByteArray(1024)
                while (zIn.read(buf).also { len = it } != -1) {
                    bos.write(buf, 0, len)
                }
                // 关流顺序，先打开的后关闭
                bos.close()
                out.close()
                success = true
            }
        }
        return success
    }

    /**
     * * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    fun copyFile(oldPath: String?, newPath: String?) {
        var inStream: InputStream? = null
        var fs: FileOutputStream? = null
        try {
            var bytesum = 0
            var byteread = 0
            val oldfile = File(oldPath)
            if (oldfile.exists()) { //文件存在时
                inStream = FileInputStream(oldPath) //读入原文件
                fs = FileOutputStream(newPath)
                val buffer = ByteArray(1444)
                while (inStream.read(buffer).also { byteread = it } != -1) {
                    bytesum += byteread //字节数 文件大小
                    println(bytesum)
                    fs.write(buffer, 0, byteread)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (inStream != null) {
                try {
                    inStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (fs != null) {
                try {
                    fs.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    fun copyFolder(oldPath: String, newPath: String) {
        try {
            File(newPath).mkdirs() //如果文件夹不存在 则建立新文件夹
            val a = File(oldPath)
            val file = a.list()
            var temp: File? = null
            for (i in file.indices) {
                temp = if (oldPath.endsWith(File.separator)) {
                    File(oldPath + file[i])
                } else {
                    File(oldPath + File.separator + file[i])
                }
                if (temp.isFile) {
                    var input: FileInputStream? = null
                    var output: FileOutputStream? = null
                    try {
                        input = FileInputStream(temp)
                        output = FileOutputStream(
                            newPath + "/" +
                                    temp.name
                        )
                        val b = ByteArray(1024 * 5)
                        var len: Int
                        while (input.read(b).also { len = it } != -1) {
                            output.write(b, 0, len)
                        }
                        output.flush()
                    } finally {
                        if (output != null) {
                            try {
                                output.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                        if (input != null) {
                            try {
                                input.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                if (temp.isDirectory) { //如果是子文件夹
                    copyFolder(
                        oldPath + "/" + file[i],
                        newPath + "/" + file[i]
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}