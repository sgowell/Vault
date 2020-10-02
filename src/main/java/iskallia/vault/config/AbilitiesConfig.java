package iskallia.vault.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.ability.*;
import iskallia.vault.ability.passive.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effects;

import java.util.Arrays;
import java.util.List;

public class AbilitiesConfig extends Config {

	@Expose private AbilityGroup<EffectAbility> HASTE;
	@Expose private AbilityGroup<EffectAbility> REGENERATION;
	@Expose private AbilityGroup<VampirismAbility> VAMPIRISM;
	@Expose private AbilityGroup<EffectAbility> RESISTANCE;
	@Expose private AbilityGroup<EffectAbility> STRENGTH;
	@Expose private AbilityGroup<EffectAbility> FIRE_RESISTANCE;
	@Expose private AbilityGroup<EffectAbility> SPEED;
	@Expose private AbilityGroup<EffectAbility> WATER_BREATHING;
	@Expose private AbilityGroup<AttributeAbility> WELL_FIT;
	@Expose private AbilityGroup<TwerkerAbility> TWERKER;
	@Expose private AbilityGroup<ElvishAbility> ELVISH;
	@Expose private AbilityGroup<AngelAbility> ANGEL;

	@Override
	public String getName() {
		return "abilities";
	}

	public List<AbilityGroup<?>> getAll() {
		return Arrays.asList(HASTE, VAMPIRISM, RESISTANCE, STRENGTH, FIRE_RESISTANCE, SPEED,
				WATER_BREATHING, WELL_FIT, TWERKER, ELVISH, ANGEL);
	}

	public AbilityGroup<?> getByName(String name) {
		return this.getAll().stream().filter(group -> group.getParentName().equals(name)).findFirst()
				.orElseThrow(() -> new IllegalStateException("Unknown ability with name " + name));
	}

	@Override
	protected void reset() {
		this.HASTE = AbilityGroup.ofEffect("Haste", Effects.HASTE, EffectAbility.Type.ICON_ONLY, 3, i -> 2);
		this.REGENERATION = AbilityGroup.ofEffect("Regeneration", Effects.REGENERATION, EffectAbility.Type.ICON_ONLY, 1, i -> 3);
		this.VAMPIRISM = new AbilityGroup<>("Vampirism", new VampirismAbility(2, 0.2F), new VampirismAbility(2, 0.4F), new VampirismAbility(2, 0.6F));
		this.RESISTANCE = AbilityGroup.ofEffect("Resistance", Effects.RESISTANCE, EffectAbility.Type.ICON_ONLY, 2, i -> 3);
		this.STRENGTH = AbilityGroup.ofEffect("Strength", Effects.STRENGTH, EffectAbility.Type.ICON_ONLY, 3, i -> 3);
		this.FIRE_RESISTANCE = AbilityGroup.ofEffect("Fire Resistance", Effects.FIRE_RESISTANCE, EffectAbility.Type.ICON_ONLY, 1, i -> 5);
		this.SPEED = AbilityGroup.ofEffect("Speed", Effects.SPEED, EffectAbility.Type.ICON_ONLY, 2, i -> 3);
		this.WATER_BREATHING = AbilityGroup.ofEffect("Water Breathing", Effects.WATER_BREATHING, EffectAbility.Type.ICON_ONLY, 1, i -> 5);
		//reach
		this.WELL_FIT = AbilityGroup.ofAttribute("Well Fit", Attributes.MAX_HEALTH, "Extra Health", 10, i -> 1, i -> i * 2.0D, i -> AttributeModifier.Operation.ADDITION);
		this.TWERKER = new AbilityGroup<>("Twerker", new TwerkerAbility(4));
		this.ELVISH = new AbilityGroup<>("Elvish", new ElvishAbility(5));
		this.ANGEL = new AbilityGroup<>("Angel", new AngelAbility(15));
	}

}
