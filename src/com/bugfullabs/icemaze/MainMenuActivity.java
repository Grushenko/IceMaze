package com.bugfullabs.icemaze;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.FadeInModifier;
import org.andengine.entity.modifier.FadeOutModifier;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.opengl.font.StrokeFont;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.texturepack.TexturePack;
import org.andengine.util.texturepack.TexturePackLoader;
import org.andengine.util.texturepack.TexturePackTextureRegion;
import org.andengine.util.texturepack.TexturePackTextureRegionLibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;
import android.view.KeyEvent;

import com.bugfullabs.icemaze.level.Level;
import com.bugfullabs.icemaze.level.LevelFileReader;
import com.bugfullabs.icemaze.util.Button;


/**
 * 
 * @author Bugful Labs
 * @author Grushenko
 * @email  wojciech@bugfullabs.pl
 *
 */


public class MainMenuActivity extends SimpleBaseGameActivity{

	private int cameraWidth;
	private int cameraHeight;
	private Camera mCamera;

	private Scene mMainScene;
	private Scene mOptionsScene; 
	
	private BitmapTextureAtlas mFontTexture;
	private StrokeFont mFont;

	private TexturePackTextureRegionLibrary mTextures;
	
	
	SharedPreferences mSettings;
	SharedPreferences.Editor mEditor;
	private static final String SETTINGS_FILE = "Settings";
	
	private boolean mToogleSound = false;
	private boolean mToogleMusic = false;
	
	private Button mMusicButton;
	private Button mSoundButton;
	
	private TexturePack mTexturePack;
	private TiledTextureRegion mButtonLongRegion;
	private TiledTextureRegion mButtonShortRegion;
	
	
	/* LEVEL GRID */
	private static final int NUMBER_OF_ITEMS = 15;
	private static final int NUMBER_OF_ITEMS_IN_ROW = 5;
	private static final float offsetX = 72;
	private static final float offsetY = 72;
	private float marginX;
	private float marginY;
	private Scene mGridScene;

	private int levelpackId = 1;

	private boolean inStart = true;
	

	@Override
	public EngineOptions onCreateEngineOptions() {
		
		Display disp = getWindowManager().getDefaultDisplay();
		
		this.cameraWidth = disp.getWidth();
		this.cameraHeight = disp.getHeight();
		
		marginX = cameraWidth/2 - ((offsetX)*(NUMBER_OF_ITEMS_IN_ROW/2));
		marginY = cameraHeight/2 - (((offsetY)*((NUMBER_OF_ITEMS/NUMBER_OF_ITEMS_IN_ROW)/2))) + offsetY + 16;
		
		mSettings = getSharedPreferences(SETTINGS_FILE, 0);
		mEditor = mSettings.edit();
		
		mToogleSound = mSettings.getBoolean("sound", false);
		mToogleMusic = mSettings.getBoolean("music", false);		

		this.mCamera = new Camera(0, 0, this.cameraWidth, this.cameraHeight);
		
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(this.cameraWidth, this.cameraHeight), this.mCamera);
	}

	@Override
	protected void onCreateResources() {


		
		
		this.mFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        Typeface typeface =  Typeface.createFromAsset(getAssets(), "font/FOO.ttf");
        mFont = new StrokeFont(this.getFontManager(), mFontTexture, typeface, 30, true, Color.WHITE, 2, Color.BLACK);
        
        TexturePackLoader tpl = new TexturePackLoader(getAssets(), getTextureManager());
        try {
		
        mTexturePack = tpl.loadFromAsset("gfx/menu/menu.xml", "gfx/menu/");
        mTexturePack.loadTexture();
        mTextures = mTexturePack.getTexturePackTextureRegionLibrary();
        } catch (Exception e) {
			e.printStackTrace();
		}
        
        TexturePackTextureRegion textureRegion = mTextures.get(GameValues.BUTTONLONG_ID);
        mButtonLongRegion = TiledTextureRegion.create(mTexturePack.getTexture(), textureRegion.getSourceX(), textureRegion.getSourceY(), textureRegion.getSourceWidth(), textureRegion.getSourceHeight(), 2, 1);
        TexturePackTextureRegion shortTextureRegion = mTextures.get(GameValues.BUTTONSHORT_ID);
        mButtonShortRegion = TiledTextureRegion.create(mTexturePack.getTexture(), shortTextureRegion.getSourceX(), shortTextureRegion.getSourceY(), shortTextureRegion.getSourceWidth(), shortTextureRegion.getSourceHeight(), 2, 1);
        
     
        mFontTexture.load();
		mFont.load();
        
	}

	@Override
	protected Scene onCreateScene() {
		this.mMainScene = new Scene();
		mMainScene.setBackground(new Background(0.34f, 0.42f, 0.73f));
		
		this.mOptionsScene = new Scene();
		mOptionsScene.setBackground(new Background(0.54f, 0.92f, 0.33f));
		
		
		setLevelSelectGrid();
		
		
		new Button(this, mMainScene, (cameraWidth/2)-125, (cameraHeight/2)-37.5f, 250, 75, getString(R.string.newgame), mButtonLongRegion, mFont){
			@Override
			public boolean onButtonPressed(){	
			inStart = false;
			changeSceneWithFade(mGridScene, 0.2f);
				
			return true;
			}
		};
		
		new Button(this, mMainScene, (cameraWidth/2)-125, (cameraHeight/2)-37.5f+75, 250, 75, getString(R.string.options), mButtonLongRegion, mFont){
			@Override
			public boolean onButtonPressed(){	
			inStart = false;
			changeSceneWithFade(mOptionsScene, 0.3f);	
				
			return true;
			}
		};
		
		new Button(this, mMainScene, (cameraWidth/2)-125, (cameraHeight/2)-37.5f+150, 250, 75, getString(R.string.exit), mButtonLongRegion, mFont){
			@Override
			public boolean onButtonPressed(){	
				MainMenuActivity.this.finish();
				return true;
			}
		};
		
		
		
		
		mMusicButton = new Button(this, mOptionsScene, (cameraWidth/2)-125, (cameraHeight/2)-37.5f, 250, 75, getString(R.string.music) + ": " + getString(R.string.yes), mButtonLongRegion, mFont){
			@Override
			public boolean onButtonPressed(){	
				mToogleMusic = !mToogleMusic;
				
				mEditor.putBoolean("music", mToogleMusic);
				mEditor.commit();
				if(mToogleMusic == true){
				this.setText(getString(R.string.music)+  ": " + getString(R.string.yes));
				}else{
				this.setText(getString(R.string.music)+  ": " + getString(R.string.no));
				}
				return true;
			}
		};
		
		
		mSoundButton = new Button(this, mOptionsScene, (cameraWidth/2)-125, (cameraHeight/2)-37.5f+75, 250, 75, getString(R.string.sound) + ": " + getString(R.string.yes), mButtonLongRegion, mFont){
			@Override
			public boolean onButtonPressed(){	
				mToogleSound = !mToogleSound;
				
				mEditor.putBoolean("sound", mToogleSound);
				mEditor.commit();
				if(mToogleSound == true){
				this.setText(getString(R.string.sound) + ": " + getString(R.string.yes));
				}else{
				this.setText(getString(R.string.sound) + ": " + getString(R.string.no));
				}
				return true;
			}
		};
		
		new Button(this, mOptionsScene, (cameraWidth/2)-125, (cameraHeight/2)-37.5f+150, 250, 75, getString(R.string.reset), mButtonLongRegion, mFont){
			@Override
			public boolean onButtonPressed(){	
				inStart = true;
				changeSceneWithFade(mMainScene, 0.3f);	
				return true;
			}
		};
		
		
		if(mToogleMusic != true)
		{
		this.mMusicButton.setText(getString(R.string.music)+  ": " + getString(R.string.no));
		}
		
		if(mToogleSound != true)
		{
			this.mSoundButton.setText(getString(R.string.sound) + ": " + getString(R.string.no));
		}
		
		
		return mMainScene;
	}
	
	
	
	private void setLevelSelectGrid() {
    	
	
	  	mGridScene = new Scene();

	  	
	  	//final Sprite bg = new Sprite(0, 0, mBackgroundTextureRegion, getVertexBufferObjectManager());
	  	//bg.setWidth(cameraWidth);
	  	//bg.setHeight(cameraHeight);
	  	//mGridScene.setBackground(new SpriteBackground(bg));
	  	mGridScene.setBackground(new Background(0.654f, 0.312f, 0.73f));
	  	


	  	  int i = 0;

	  	  
	  	  for(int j = 0; j < (NUMBER_OF_ITEMS/NUMBER_OF_ITEMS_IN_ROW); j++){

				for(int k = 0; k < NUMBER_OF_ITEMS_IN_ROW; k++){

				final int id = i + 1;
				
				new Button(this, mGridScene,  marginX + (k * offsetX) - 36, marginY + (j * offsetY)- 36, 72, 72, Integer.toString(id),  mButtonShortRegion, mFont){
	    			  @Override
	    			  public boolean onButtonPressed(){
	    				MainMenuActivity.this.onLevelSelected(id, levelpackId);
	    				return true;
	    			  }
	    		  };
	    		  
	    		  i++;
				}
			}


	  	  this.mEngine.setScene(mGridScene);

	    }



	    private void onLevelSelected(int id, int level_pack)
	    {


	    	try {
			  	final Level level = LevelFileReader.getLevelFromFile(this, "level_"+ Integer.toString(level_pack) + "_" + Integer.toString(id));

			  	GameActivity.setLevel(level);

			  	this.startActivity(new Intent(this, GameActivity.class));
			  	overridePendingTransition(R.anim.fadein, R.anim.fadeout);
			  	
			} catch (Exception e) {
				e.printStackTrace();
			}

	    }
	
	
	    @Override
		public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
			if(pKeyCode == KeyEvent.KEYCODE_BACK  && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
				
				if(!inStart){
				changeSceneWithFade(mMainScene, 0.2f);	
				inStart = true;
				}else{
				MainMenuActivity.this.finish();
				}
				
				
				return true;
			}
			return super.onKeyDown(pKeyCode, pEvent); 
		}

	
	
	
	private void changeSceneWithFade(final Scene s, final float time){
	Scene cs = this.getEngine().getScene();

	final Rectangle black = new Rectangle(0, 0, cameraWidth, cameraHeight, this.getVertexBufferObjectManager());
	black.setColor(0.0f, 0.0f, 0.0f);
	black.setAlpha(0.0f);
	
	cs.attachChild(black);
	
	black.registerEntityModifier(new FadeInModifier(time, new IEntityModifierListener() {
		
		@Override
		public void onModifierStarted(IModifier<IEntity> arg0, IEntity arg1) {	
		}
		
		@Override
		public void onModifierFinished(IModifier<IEntity> arg0, IEntity arg1) {
		
			black.detachSelf();
			s.attachChild(black);
			MainMenuActivity.this.getEngine().setScene(s);
			black.registerEntityModifier(new FadeOutModifier(time, new IEntityModifierListener() {
				
				@Override
				public void onModifierStarted(IModifier<IEntity> arg0, IEntity arg1) {
				}
				
				@Override
				public void onModifierFinished(IModifier<IEntity> arg0, IEntity arg1) {
					black.detachSelf();
				}
			}));
		}
	}));
	
	
	}
	
}