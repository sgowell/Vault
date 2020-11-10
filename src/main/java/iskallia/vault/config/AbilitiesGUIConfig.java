package iskallia.vault.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.client.gui.helper.SkillFrame;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.init.ModConfigs;

import java.util.HashMap;

public class AbilitiesGUIConfig extends Config {

    @Expose private HashMap<String, SkillStyle> styles;

    @Override
    public String getName() {
        return "abilities_gui_styles";
    }

    public HashMap<String, SkillStyle> getStyles() {
        return styles;
    }

    @Override
    protected void reset() {
        SkillStyle style;
        this.styles = new HashMap<>();

        style = new SkillStyle(0, 0,
                16 * 4, 0);
        style.frameType = SkillFrame.RECTANGULAR;
        styles.put(ModConfigs.ABILITIES.NIGHT_VISION.getParentName(), style);

        style = new SkillStyle(50, 0,
                16 * 6, 0);
        style.frameType = SkillFrame.RECTANGULAR;
        styles.put(ModConfigs.ABILITIES.INVISIBILITY.getParentName(), style);
    }

}
