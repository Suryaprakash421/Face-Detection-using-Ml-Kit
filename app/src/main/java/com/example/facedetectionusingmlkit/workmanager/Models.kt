package com.example.facedetectionusingmlkit.workmanager

object Models {
    val FACENET_512_F16 = ModelInfo(
        "FaceNet-512 f16",
        "facenet_512_fp16.tflite",
        0.3f,
        23.56f,
        512,
        160
    )

//    val FACENET_512_QUANTIZED = ModelInfo(
//        "FaceNet-512 Quantized",
//        "facenet_512_int_quantized.tflite",
//        0.3f,
//        23.56f,
//        512,
//        160
//    )
}