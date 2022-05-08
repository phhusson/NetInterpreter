#!/bin/bash


# See camera.kt for reference minized kt code
picture() {
    (
        # Store context in var `context`
        echo 'DUP ~context'
        echo '"camera INV :getSystemService(java.lang.String) ~camera'

        # Create a listener for opening camera
        echo '90android.hardware.camera2.CameraDevice$StateCallback ~cameralistener'
        echo '91android.hardware.camera2.CameraDevice$StateCallback.onOpened'

        # Open camera
        echo '=handler =cameralistener "0 =camera :openCamera(java.lang.String,android.hardware.camera2.CameraDevice$StateCallback,android.os.Handler)'

        # Wait for camera opened event
        echo '92android.hardware.camera2.CameraDevice$StateCallback.onOpened'
        echo '[0 ~cameraDevice'

        # Grab ImageFormat.JPEG
        echo '1android.graphics.ImageFormat .JPEG ~JPEG'
        # val imageReader = ImageReader.newInstance(w, h, JPEG, 4)
        echo '0d4 =JPEG 0d1080 0d1920 1android.media.ImageReader :newInstance(int,int,int,int) ~imagereader'
        echo '=imagereader :getSurface() ~imagereader.surface'

        # Create a SessionConfiguration
        #SESSION_REGULAR type
        echo '1android.hardware.camera2.params.SessionConfiguration .SESSION_REGULAR ~SESSION_REGULAR'

        # List(OutputConfiguration(imageReader.surface))
        echo '=imagereader.surface 1android.hardware.camera2.params.OutputConfiguration +(android.view.Surface) ~outputConfiguration'
        echo '=outputConfiguration 1java.util.List :of(java.lang.Object) ~outputConfigurations'

        # Make a listener
        echo '90android.hardware.camera2.CameraCaptureSession$StateCallback ~capturelistener'
        echo '91android.hardware.camera2.CameraCaptureSession$StateCallback.onConfigured'


        # Construct SessionConfiguration
        echo '=capturelistener'
        echo '=executor'
        echo '=outputConfigurations'
        echo '=SESSION_REGULAR'
        echo '1android.hardware.camera2.params.SessionConfiguration +SessionConfiguration(int,java.util.List,java.util.concurrent.Executor,android.hardware.camera2.CameraCaptureSession$StateCallback) ~sessionConfiguration'

        # cameraDevice.createCaptureSession
        echo '=sessionConfiguration =cameraDevice :createCaptureSession(android.hardware.camera2.params.SessionConfiguration)'
        echo '92android.hardware.camera2.CameraCaptureSession$StateCallback.onConfigured'

        echo '[0 ~CameraCaptureSession'

        #val captureRequest = cam.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        echo '1android.hardware.camera2.CameraDevice .TEMPLATE_STILL_CAPTURE =cameraDevice :createCaptureRequest(int) ~captureRequest'
        # captureRequest.addTarget(imageReader.surface)
        echo '=imagereader.surface =captureRequest :addTarget(android.view.Surface)'
        # captureRequest.build()
        echo '=captureRequest :build() ~captureRequest'


        # Make a CaptureCallback
        echo '90android.hardware.camera2.CameraCaptureSession$CaptureCallback ~captureCallback'
        echo '91android.hardware.camera2.CameraCaptureSession$CaptureCallback.onCaptureCompleted'

        # cameraCaptureSession.capture()
        echo '=handler =captureCallback =captureRequest =CameraCaptureSession :capture(android.hardware.camera2.CaptureRequest,android.hardware.camera2.CameraCaptureSession$CaptureCallback,android.os.Handler)'

        # Wait for the image
        echo '92android.hardware.camera2.CameraCaptureSession$CaptureCallback.onCaptureCompleted'

        echo '=imagereader :acquireNextImage() ~img'
        # Get jpeg into a DirectByteBuffer `buffer`
        echo '=img :getPlanes() [0 INSPECT :getBuffer() ~buffer'
        # Copy it into a byte[] `bufferArray`
        echo '=buffer :capacity() NEW_BYTE_ARRAY ~bufferArray'
        echo '=bufferArray =buffer :get(byte[])'

        # Open /sdcard/Android/me.phh.netinterpreter/cache/hello.jpg
        echo '=context :getExternalCacheDirs() [0 ~cacheDir'
        echo '"hello.jpg =cacheDir 1java.io.File +(java.io.File,java.lang.String) ~outFile'
        echo '=outFile 1java.io.FileOutputStream +(java.io.File) ~outFd'
        # Write bufferArray to it
        echo '=bufferArray =outFd :write(byte[])'
        # Flush and close
        echo '=outFd :flush() =outFd :close()'

        echo 'STACK'

        # Close connection
        echo 'EXIT'
    ) | nc -v localhost 9988
}
