package org.andengine.examples;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.examples.adt.ZoomState;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.compressed.pvr.PVRTexture;
import org.andengine.opengl.texture.compressed.pvr.PVRTexture.PVRTextureFormat;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.util.debug.Debug;

import android.opengl.GLES20;
import android.widget.Toast;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 *
 * @author Nicolas Gramlich
 * @since 11:54:51 - 13.07.2011
 */
public class PVRTextureExample extends BaseExample {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private ITexture mTextureRGB565;
	private ITexture mTextureRGBA5551;
	private ITexture mTextureARGB4444;
	private ITexture mTextureRGBA888MipMaps;

	private ITextureRegion mHouseNearestTextureRegion;
	private ITextureRegion mHouseLinearTextureRegion;
	private ITextureRegion mHouseMipMapsNearestTextureRegion;
	private ITextureRegion mHouseMipMapsLinearTextureRegion;

	private ZoomState mZoomState = ZoomState.NONE;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {
		Toast.makeText(this, "The lower houses use MipMaps!", Toast.LENGTH_LONG);
		Toast.makeText(this, "Click the top half of the screen to zoom in or the bottom half to zoom out!", Toast.LENGTH_LONG);

		final Camera camera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 0, 0, 0.1f) {
			@Override
			public void onUpdate(final float pSecondsElapsed) {
				switch (PVRTextureExample.this.mZoomState) {
					case IN:
						this.setZoomFactor(this.getZoomFactor() + 0.1f * pSecondsElapsed);
						break;
					case OUT:
						this.setZoomFactor(this.getZoomFactor() - 0.1f * pSecondsElapsed);
						break;
				}
				super.onUpdate(pSecondsElapsed);
			}
		};

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() {
		try {
			this.mTextureRGB565 = new PVRTexture(PVRTextureFormat.RGB_565, new TextureOptions(GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE, false)) {
				@Override
				protected InputStream onGetInputStream() throws IOException {
					return PVRTextureExample.this.getResources().openRawResource(R.raw.house_pvr_rgb_565);
				}
			}.load(this.getTextureManager());

			this.mTextureRGBA5551 = new PVRTexture(PVRTextureFormat.RGBA_5551, new TextureOptions(GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE, false)) {
				@Override
				protected InputStream onGetInputStream() throws IOException {
					return PVRTextureExample.this.getResources().openRawResource(R.raw.house_pvr_argb_5551);
				}
			}.load(this.getTextureManager());

			this.mTextureARGB4444 = new PVRTexture(PVRTextureFormat.RGBA_4444, new TextureOptions(GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE, false)) {
				@Override
				protected InputStream onGetInputStream() throws IOException {
					return PVRTextureExample.this.getResources().openRawResource(R.raw.house_pvr_argb_4444);
				}
			}.load(this.getTextureManager());

			this.mTextureRGBA888MipMaps = new PVRTexture(PVRTextureFormat.RGBA_8888, new TextureOptions(GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE, false)) {
				@Override
				protected InputStream onGetInputStream() throws IOException {
					return PVRTextureExample.this.getResources().openRawResource(R.raw.house_pvr_argb_8888_mipmaps);
				}
			}.load(this.getTextureManager());

			this.mHouseNearestTextureRegion = TextureRegionFactory.extractFromTexture(this.mTextureRGB565, 0, 0, 512, 512);
			this.mHouseLinearTextureRegion = TextureRegionFactory.extractFromTexture(this.mTextureRGBA5551, 0, 0, 512, 512);
			this.mHouseMipMapsNearestTextureRegion = TextureRegionFactory.extractFromTexture(this.mTextureARGB4444, 0, 0, 512, 512);
			this.mHouseMipMapsLinearTextureRegion = TextureRegionFactory.extractFromTexture(this.mTextureRGBA888MipMaps, 0, 0, 512, 512);
		} catch (final Throwable e) {
			Debug.e(e);
		}
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();
		scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

		final int centerX = CAMERA_WIDTH / 2;
		final int centerY = CAMERA_HEIGHT / 2;

		final Entity container = new Entity(centerX, centerY);
		container.setScale(0.5f);

		container.attachChild(new Sprite(-512, -384, this.mHouseNearestTextureRegion));
		container.attachChild(new Sprite(0, -384, this.mHouseLinearTextureRegion));
		container.attachChild(new Sprite(-512, -128, this.mHouseMipMapsNearestTextureRegion));
		container.attachChild(new Sprite(0, -128, this.mHouseMipMapsLinearTextureRegion));

		scene.attachChild(container);

		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
				if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()) {
					if(pSceneTouchEvent.getY() < CAMERA_HEIGHT / 2) {
						PVRTextureExample.this.mZoomState = ZoomState.IN;
					} else {
						PVRTextureExample.this.mZoomState = ZoomState.OUT;
					}
				} else {
					PVRTextureExample.this.mZoomState = ZoomState.NONE;
				}
				return true;
			}
		});

		return scene;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
