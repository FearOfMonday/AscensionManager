package AscensionExtra.buttons;

import AscensionExtra.AscensionMod;
import AscensionExtra.util.TexLoader;
import basemod.ClickableUIElement;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;

import java.util.Random;

import static AscensionExtra.AscensionMod.*;

public class AscensionData extends ClickableUIElement {
    private static Texture highlightImg = null;
    public String name;
    public String id;
    public String[] ascInfo;
    public int uniqueCounter;
    public boolean clicked;
    private boolean hovered;
    private int unlockedLvls;
    private boolean locked;
    private Color textColor;
    private final Color imageColor;
    private final Color highlightColor = new Color(1.0F, 1.0F, 1.0F, 0.0F);
    private final AscensionMod.AscensionManager manager;

    public AscensionData(String img, String id, String name, String[] ascInfo, boolean locked, AscensionMod.AscensionManager manager) {
        super((Texture) null, 0.0F, 0.0F, 350.0F, 40.0F);
        this.id = id;
        this.ascInfo = ascInfo;
        this.name = name;
        this.locked = locked;
        this.manager = manager;
        if (img != null && !img.equals("")) {
            image = TexLoader.getTexture(img);
            imageColor = Color.WHITE;
        } else {
            image = TexLoader.getTexture(makePath("images/default_asc.png"));
            long lo = 0;
            if (id != null && !id.equals("")) {
                for (char ch : id.toCharArray()) {
                    lo = 50*lo + ch + manager.getSize();
                }
            }
            Random rng = new Random(lo);
            imageColor = new Color(rng.nextFloat(), rng.nextFloat(), rng.nextFloat(), 1.0F);
        }
        unlockedLvls = locked ? 1 : ascInfo.length;
        uniqueCounter = 0;
        clicked = false;
        hovered = false;
        textColor = Settings.CREAM_COLOR;
        addNumbersToAscInfo();
        trimName();
        if (highlightImg == null) highlightImg = ImageMaster.loadImage("images/ui/mainMenu/menu_option_highlight.png");
    }

    private void addNumbersToAscInfo() {
        for (int i = 0; i < ascInfo.length; i++) {
            if (i != ascInfo.length - 1) ascInfo[i] = (i+1) + ". " + ascInfo[i];
            else ascInfo[i] = (i+1) + ". " + AscensionMod.TEXT[1] + ascInfo[i];
        }
    }

    private void trimName() {
        if (name == null || name.length() == 0) {
            name = "Error";
        } else {
            if (name.length() > 20) {
                name = name.substring(0, 20);
            }
        }
    }

    public void updateUnlockable() {
        if (unlockedLvls < ascInfo.length && uniqueCounter == unlockedLvls) {
            try {
                SpireConfig config = new SpireConfig(MOD_ID, "am-" + id.replace(":", "") + "-config");
                config.load();
                config.setInt("ascensionmanager:" + AscensionMod.p.name() + "_ul", unlockedLvls + 1);
                config.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveLvl() {
        if (locked && uniqueCounter > unlockedLvls) uniqueCounter = unlockedLvls;
        if (uniqueCounter > ascInfo.length) uniqueCounter = ascInfo.length;
        if (uniqueCounter <= 0) uniqueCounter = 0;
        if (AscensionMod.p != null) {
            try {
                SpireConfig config = new SpireConfig(MOD_ID, "am-" + id.replace(":", "") + "-config");
                config.load();
                config.setInt("ascensionmanager:" + AscensionMod.p.name() + "_uc", uniqueCounter);
                config.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadData() {
        uniqueCounter = 0;
        unlockedLvls = locked ? 1 : ascInfo.length;
        if (AscensionMod.p != null) {
            try {
                SpireConfig config = new SpireConfig(MOD_ID, "am-" + id.replace(":", "") + "-config");
                config.load();
                if (config.has("ascensionmanager:" + AscensionMod.p.name() + "_ul")) unlockedLvls = config.getInt("ascensionmanager:" + AscensionMod.p.name() + "_ul");
                if (config.has("ascensionmanager:" + AscensionMod.p.name() + "_uc")) uniqueCounter = config.getInt("ascensionmanager:" + AscensionMod.p.name() + "_uc");
                if (unlockedLvls > ascInfo.length) unlockedLvls = ascInfo.length;
                if (uniqueCounter > unlockedLvls) uniqueCounter = unlockedLvls;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onHover() {
        hovered = true;
    }

    @Override
    protected void onUnhover() {
        hovered = false;
    }

    @Override
    protected void onClick() {
        clicked = !clicked;
        if (clicked) {
            manager.disableButtons(this);
            saveLvl();
            setLvlAndText();
        } else manager.resetTxtNLvl();
    }

    public void setLvlAndText() {
        CardCrawlGame.mainMenuScreen.charSelectScreen.ascLevelInfoString = uniqueCounter > 0 ? ascInfo[uniqueCounter - 1] : AscensionMod.TEXT[0];
    }

    public void unlock() {
        locked = false;
        int tmp = uniqueCounter;
        unlockedLvls = ascInfo.length - 1;
        uniqueCounter = unlockedLvls;
        for (AbstractPlayer.PlayerClass p : AbstractPlayer.PlayerClass.values()) {
            AscensionMod.p = p;
            updateUnlockable();
        }
        AscensionMod.p = null;
        uniqueCounter = tmp;
    }

    @Override
    public void update() {
        super.update();
        if (clicked && isClickable()) {
            textColor = Settings.GREEN_TEXT_COLOR;
            highlightColor.a = 0.5F;
        } else {
            clicked = false;
            textColor = Settings.CREAM_COLOR;
            highlightColor.a = MathHelper.fadeLerpSnap(highlightColor.a, 0.0F);
        }
        if (hovered) textColor = Settings.GREEN_TEXT_COLOR;
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        sb.setColor(highlightColor);
        sb.draw(highlightImg, x - 26 * Settings.scale, y - 18 * Settings.scale, 460 * Settings.scale, 78 * Settings.scale);
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sb.setColor(imageColor.cpy());
        sb.draw(image, x + 14 * Settings.scale, y + 2 * Settings.scale, image.getWidth() * Settings.scale, image.getHeight() * Settings.scale);
        sb.setColor(Color.WHITE);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.cardTitleFont, name, x + 60 * Settings.scale, y + 30 * Settings.scale, textColor);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.cardTitleFont, String.valueOf(uniqueCounter), x + 360 * Settings.scale, y + 30 * Settings.scale, Color.SKY);
        renderHitbox(sb);
    }
}
