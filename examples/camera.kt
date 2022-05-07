val camMgr = getSystemService(CameraManager::class.java)
val camera = camMgr.openCamera("0", object: CameraDevice.StateCallback() {
    override fun onOpened(p0: CameraDevice) {
        val imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 32)
        val imReaderSurface = imageReader.surface
        val sessionConfiguration = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            listOf(OutputConfiguration(imReaderSurface)),
            executor,
            object: CameraCaptureSession.StateCallback() {
                override fun onConfigured(p1: CameraCaptureSession) {
                    val captureRequest =
                        p0.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)
                    captureRequest.addTarget(imReaderSurface)
                    val captureCallback = object :
                        CameraCaptureSession.CaptureCallback() {
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult
                        ) {
                            val img = imageReader.acquireNextImage()
                            val b = img.planes[0].buffer
                            val bb = ByteArray(b.capacity())
                            b.get(bb)
                            FileOutputStream(File(externalCacheDirs[0], "test.jpg")).use {
                                it.write(bb)
                            }
                        }
                    }
                }
            })
        p0.createCaptureSession(sessionConfiguration)
    }
}
