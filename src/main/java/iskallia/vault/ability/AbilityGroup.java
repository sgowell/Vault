package iskallia.vault.ability;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.annotations.Expose;
import iskallia.vault.ability.passive.AttributeAbility;
import iskallia.vault.ability.passive.EffectAbility;
import iskallia.vault.util.RomanNumber;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;

import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

public class AbilityGroup<T extends PlayerAbility> {

	@Expose private final String name;
	@Expose private final T[] levels;

	private BiMap<String, T> registry;

	public AbilityGroup(String name, T... levels) {
		this.name = name;
		this.levels = levels;
	}

	public int getLevels() {
		return this.levels.length;
	}

	public String getParentName() {
		return this.name;
	}

	public String getName(int level) {
		return this.getRegistry().inverse().get(this.getAbility(level));
	}

	public T getAbility(int level) {
		return this.levels[level];
	}

	public BiMap<String, T> getRegistry() {
		if(this.registry == null) {
			this.registry = HashBiMap.create(this.getLevels());

			if(this.getLevels() == 1) {
				this.registry.put(this.getParentName(), this.levels[0]);
			} else if(this.getLevels() > 1) {
				for(int i = 0; i < this.getLevels(); i++) {
					this.registry.put(this.getParentName() + " " + RomanNumber.toRoman(i + 1), this.getAbility(i));
				}
			}
		}

		return this.registry;
	}

	public static AbilityGroup<EffectAbility> ofEffect(String name, Effect effect, EffectAbility.Type type, int levels,
                                                       IntUnaryOperator cost) {
		EffectAbility[] abilities = IntStream.range(0, levels)
				.mapToObj(i -> new EffectAbility(cost.applyAsInt(i + 1), effect, i, type)).toArray(EffectAbility[]::new);
		return new AbilityGroup<>(name, abilities);
	}

	public static AbilityGroup<AttributeAbility> ofAttribute(String name, Attribute attribute, String modifierName,
                                                             int levels, IntUnaryOperator cost, IntToDoubleFunction amount,
                                                             IntFunction<AttributeModifier.Operation> operation) {
		AttributeAbility[] abilities = IntStream.range(0, levels)
				.mapToObj(i -> new AttributeAbility(cost.applyAsInt(i + 1), attribute, new AttributeAbility.Modifier(
						modifierName + " " + RomanNumber.toRoman(i + 1),
						amount.applyAsDouble(i + 1),
						operation.apply(i + 1)
				))).toArray(AttributeAbility[]::new);
		return new AbilityGroup<>(name, abilities);
	}

}
