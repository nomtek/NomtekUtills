[![](https://jitpack.io/v/nomtek/NomtekUtills.svg)](https://jitpack.io/#nomtek/NomtekUtills)

# CenterCropVideoView (min sdk version - 16 )
View that supports playing videos in center crop mode. CenterCropVideoView is based on VideoView from
the official Android sources for 7.1.1_r13. The main difference is that it extends
[android.view.TextureView](https://developer.android.com/reference/android/view/TextureView) instead of a
[android.view.SurfaceView](https://developer.android.com/reference/android/view/SurfaceView). This change
lets us to use method [setTransform(Matrix matrix)](https://developer.android.com/reference/android/view/TextureView.html#setTransform(android.graphics.Matrix))
to apply correct scaling to the video.

<img src="../resources/centercropvideoview.gif" width="250">

### How to use - our [sample](https://github.com/nomtek/NomtekUtills/tree/master/app/src/main/java/com/nomtek/centercropvideoview/example)
##### 1. Add CenterCropVideoView to your activity layout.
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.nomtek.libs.centercropvideoview.CenterCropVideoView
        android:id="@+id/centerCropVideoView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</FrameLayout>

```

##### 2. Start playing video. Pause the video when application goes to the background and resume when it comes back to the foreground
```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_crop_video)
        val path = "android.resource://${this.packageName}/${R.raw.sample}"
        centerCropVideoView.setVideoURI(Uri.parse(path))
        centerCropVideoView.setOnPreparedListener(MediaPlayer.OnPreparedListener {
            it.start()
        })

    }

    override fun onResume() {
        super.onResume()
        centerCropVideoView.start()
    }

    override fun onPause() {
        super.onPause()
        centerCropVideoView.pause()
    }

```


##### 3. License
```
 Copyright (C) 2006 The Android Open Source Project

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.


 Modifications copyright (C) 2014-2016 sprylab technologies GmbH
 Modifications copyright (C) 2018 Nomtek
```



