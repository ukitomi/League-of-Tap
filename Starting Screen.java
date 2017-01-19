package com.mygdx.game;

/**
 * The starting screen of LoT
 * @author QiQi Yuki Ou, Justin Tennent, Andre Chik
 */
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.mygdx.game.builders.LevelBuilder;
import com.mygdx.game.units.Creep;
import com.mygdx.game.units.Enemy;

import java.util.LinkedList;

public class ActualGame implements Screen {

	MyGdxGame game;
	OrthographicCamera camera;
	public SpriteBatch batch; // contains/renders gameplay graphics (non-UI)
	public Stage stage; // stage holds the Button
	public BitmapFont font; // same as that used in Tut 7
	public TextureAtlas buttonsAtlas; // image of button
	public Skin buttonSkin; // images are used as skins of the buttom
	public FreeTypeFontGenerator generator; // font generator
	public TextButton backButton, timer;
	public long startTime, currTime;
	public Image xpBar;
	public Music music; // background music
	public Creep c; // debugging creep
	public Player player;

	public LinkedList<Level> levels;
	public int currLevel;
	public int deathToll;

	public ActualGame(final MyGdxGame game) {
		this.game = game;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1080, 1920);
	}

	public void render(float x) {
		// Clear screen, set background to black
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		// font.draw(batch, tapInfoString, 0, 1000); // Display touch debug info
		currTime = System.currentTimeMillis();
		timer.setText(((currTime - startTime) / 1000) + ":" + ((currTime - startTime) % 1000));

		// If player has completed all levels
		if (currLevel == 30) {
			game.setScreen(new PlayerMenu(game));
		}

		// Check if all enemies have been killed; if so, load next level
		if (GameVariables.DeathToll >= levels.get(currLevel).getEnemyCount()) {
			currLevel++;
			GameVariables.DeathToll = 0;
			for (Enemy e : levels.get(currLevel).getEnemyList()) {
				stage.addActor(e);
				e.setListener(player);
			}
			Gdx.app.log("debug creep", "loaded level " + currLevel);
		}
	}

	public void dispose() {
		batch.dispose();
		buttonSkin.dispose();
		buttonsAtlas.dispose();
		music.dispose();
		stage.dispose();
		generator.dispose();
	}

	public void show() {
		buttonsAtlas = new TextureAtlas("buttons/actualGame.pack"); // button
																	// atlas
																	// image
		buttonSkin = new Skin();
		buttonSkin.addRegions(buttonsAtlas);
		font = new BitmapFont(Gdx.files.internal("fonts/CustomFont.fnt"), false); // the
																					// font

		stage = new Stage(); // Initialize UI
		stage.clear();

		player = new Player();

		TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(); // back
																				// button
																				// properties
		style.up = buttonSkin.getDrawable("pauseicon");
		style.down = buttonSkin.getDrawable("pauseicon");
		style.font = font;

		TextButton.TextButtonStyle upperStyle = new TextButton.TextButtonStyle(); // upper
																					// ribbon
																					// for
																					// timer
		upperStyle.up = buttonSkin.getDrawable("ribbon");
		upperStyle.down = buttonSkin.getDrawable("ribbon");
		upperStyle.font = font;

		TextButton.TextButtonStyle lowerStyle = new TextButton.TextButtonStyle(); // lower
																					// ribbon
																					// for
																					// gold
																					// and
																					// EXP
		lowerStyle.up = buttonSkin.getDrawable("ribbon");
		lowerStyle.down = buttonSkin.getDrawable("ribbon");
		lowerStyle.font = font;

		generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/leaguetitlefont.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 108;
		parameter.shadowColor = Color.GRAY;
		parameter.shadowOffsetX = 5;
		parameter.shadowOffsetY = 5;
		BitmapFont font12 = generator.generateFont(parameter);
		TextButton.TextButtonStyle timerStyle = new TextButton.TextButtonStyle(); // Shop
																					// title
																					// ribbon
																					// properties
		timerStyle.font = font12; // the text style font for the shop title
									// ribbon image

		timer = new TextButton("00:00", timerStyle); // timer
		timer.setPosition(200, 1720);
		timer.setSize(880, 200);
		timer.setColor(Color.WHITE);

		backButton = new TextButton("", style); // button text and style
		backButton.setPosition(0, 1720); // button location
		backButton.setSize(200, 200);
		backButton.setZIndex(4);

		TextButton upper = new TextButton("", lowerStyle);
		upper.setPosition(200, 1720);
		upper.setSize(1080, 200);
		upper.setZIndex(3);

		TextButton lower = new TextButton("", lowerStyle);
		lower.setPosition(0, 0);
		lower.setSize(1080, 250);
		lower.setZIndex(3);

		Image background = new Image(new Texture("game/background1.jpg"));
		background.setSize(1920, 1080);
		background.setScale(2);
		background.setZIndex(0);
		background.setPosition(-500, 250);

		xpBar = new Image(new Texture("game/hp-foreground.png"));
		xpBar.setColor(Color.BLUE);
		xpBar.setPosition(0, 300);
		GameVariables.xpWidth = 0;
		xpBar.setWidth(0);

		stage.addActor(xpBar);
		stage.addActor(background);
		stage.addActor(upper);
		stage.addActor(lower);
		stage.addActor(backButton);
		stage.addActor(timer);

		Gdx.input.setInputProcessor(stage);

		backButton.addListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				// gameButton.setBounds(-250, 500, 100, 100);
				Gdx.app.log("pause icon", "pressed");
				music.stop();
				game.setScreen(new PlayerMenu(game));
				return true;
			}

			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				Gdx.app.log("pause icon", "released");
			}
		});

		// Generate level list
		levels = LevelBuilder.newLevelList(player);
		deathToll = 0;
		currLevel = 0;

		for (Enemy e : levels.get(currLevel).getEnemyList()) {
			e.setListener(player);
			e.setZIndex(1);
			stage.addActor(e);
			Gdx.app.log("debug", "added listener to initial creep");
		}

		music = Gdx.audio.newMusic(Gdx.files.internal("game/challengers.mp3"));
		music.play();

		startTime = System.currentTimeMillis();
	}

	public void hide() {
	}

	public void pause() {
	}

	public void resume() {
	}

	public void resize(int x, int y) {
	}

}
