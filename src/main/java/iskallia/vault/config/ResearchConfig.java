package iskallia.vault.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.research.Restrictions;
import iskallia.vault.research.type.CustomResearch;
import iskallia.vault.research.type.ModResearch;
import iskallia.vault.research.type.Research;

import java.util.LinkedList;
import java.util.List;

public class ResearchConfig extends Config {

    @Expose public List<ModResearch> MOD_RESEARCHES;
    @Expose public List<CustomResearch> CUSTOM_RESEARCHES;

    @Override
    public String getName() {
        return "researches";
    }

    public List<Research> getAll() {
        List<Research> all = new LinkedList<>();
        all.addAll(MOD_RESEARCHES);
        all.addAll(CUSTOM_RESEARCHES);
        return all;
    }

    public Research getByName(String name) {
        for (Research research : getAll()) {
            if (research.getName().equals(name))
                return research;
        }
        return null;
    }

    @Override
    protected void reset() {
        this.MOD_RESEARCHES = new LinkedList<>();
        this.MOD_RESEARCHES.add(new ModResearch("Backpacks!", 2, "simplybackpacks").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Waystones", 3, "waystones").withRestrictions(false, false, true, true, true));
        this.MOD_RESEARCHES.add(new ModResearch("Safety First", 3, "torchmaster").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Organisation", 3, "trashcans", "dankstorage", "pickletweaks").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Super Builder", 3, "buildinggadgets").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Super Miner", 8, "mininggadgets").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Storage Noob", 1, "ironchest", "metalbarrels").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Storage Master", 2, "storage_overhaul", "storagedrawers", "modularrouters").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Storage Refined", 6, "refinedstorage").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Storage Energistic", 6, "appliedenergistics").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Storage Enthusiast", 4, "rftoolsstorage").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Decorator", 1, "decorative_blocks", "camera", "masonry").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Decorator Pro", 2, "mcwbridges", "mcwdoors", "mcwroofs", "mcwwindows", "enviromats", "blockcarpentry", "platforms").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Engineer", 1, "ironfurnaces", "engineersdecor").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Super Engineer", 3, "movingelevators", "immersiveengineering").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("One with Ender", 1, "endermail", "elevatorid").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("The Chef", 1, "cookingforblockheads").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Traveller", 1, "comforts").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Adventurer", 3, "dimstorage").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Hacker", 6, "xnet").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Redstoner", 1, "rsgauges", "rftoolsutility").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Natural Magical", 8, "botania").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Tech Freak", 10, "mekanism").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("The Emerald King", 3, "easy_villagers").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Quarry", 6, "rftoolsbuilder").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Spaceman", 4, "ironjetpacks").withRestrictions(false, false, false, false, true));
        this.MOD_RESEARCHES.add(new ModResearch("Total Control", 3, "darkutils").withRestrictions(false, false, false, false, true));

        this.MOD_RESEARCHES.add(new ModResearch("Let there be light!", 2, ""));
        this.MOD_RESEARCHES.add(new ModResearch("Energetic", 2, ""));
        this.MOD_RESEARCHES.add(new ModResearch("Thermal Technician", 10, ""));
        this.MOD_RESEARCHES.add(new ModResearch("Plastic Technician", 8, ""));
        this.MOD_RESEARCHES.add(new ModResearch("Extended Possibilities", 3, ""));
        this.MOD_RESEARCHES.add(new ModResearch("Power Overwhelming", 6, ""));
        this.MOD_RESEARCHES.add(new ModResearch("Nuclear Power", 6, ""));
        this.MOD_RESEARCHES.add(new ModResearch("Automatic Genius", 20, ""));

        this.CUSTOM_RESEARCHES = new LinkedList<>();
        CustomResearch sampleResearch = new CustomResearch("Pickaxe Proficiency I", 1);
        sampleResearch.getItemRestrictions().put(
                "minecraft:wooden_pickaxe", Restrictions.forItems()
                        .set(Restrictions.Type.USABILITY, true)
                        .set(Restrictions.Type.HITTABILITY, true));
        sampleResearch.getBlockRestrictions().put(
                "minecraft:stone", Restrictions.forBlocks());
        sampleResearch.getEntityRestrictions().put(
                "minecraft:creeper", Restrictions.forEntities());
        this.CUSTOM_RESEARCHES.add(sampleResearch);
    }

}
