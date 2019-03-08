/*
 * Copyright (C) 2013 yixia.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vov.vitamio.demo;


import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class VideoViewDemo extends Activity {

	/**
	 * TODO: Set the path variable to a streaming video URL or a local media file
	 * path.
	 */

	boolean ifUpdate;;
	

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Vitamio.isInitialized(this);
		
		setContentView(R.layout.videoview);

		playfunction();	

	}

	
	void playfunction(){
		 String path = "";
		 VideoView mVideoView;
		 EditText mEditText;
		mEditText = (EditText) findViewById(R.id.url);
		mVideoView = (VideoView) findViewById(R.id.surface_view);

		path="http://ips.ifeng.com/video19.ifeng.com/video09/2019/02/21/p10620914-102-009-100537.mp4?vid=bbf26dc0-aa64-44df-9af8-36702c3789b0&uid=1550714542778_a72rd28050&from=v_Free&pver=vHTML5Player_v2.0.0&sver=&se=%E9%82%A3%E4%BA%9B%E8%B6%85%E7%89%9B%E7%9A%84%E8%80%81%E5%A4%96%E4%BB%AC&cat=57-60&ptype=57&platform=pc&sourceType=h5&dt=1550714693000&gid=oBJU4WkN48mp&sign=14f0ccb891fa320dc8bdfe6b3388c43a&tm=1550716225969";
      if (path == "") {
			// Tell the user to provide a media file URL/path.
			Toast.makeText(VideoViewDemo.this, "Please edit VideoViewDemo Activity, and set path" + " variable to your media file URL/path", Toast.LENGTH_LONG).show();
			return;
		} else {
			/*
			 * Alternatively,for streaming media you can use
			 * mVideoView.setVideoURI(Uri.parse(URLstring));
			 */
			mVideoView.setVideoPath(path);
			mVideoView.setMediaController(new MediaController(this));
			mVideoView.requestFocus();

			mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mediaPlayer) {
					// optional need Vitamio 4.0
					mediaPlayer.setPlaybackSpeed(1.0f);
				}
			});
		}
	}
	
}
