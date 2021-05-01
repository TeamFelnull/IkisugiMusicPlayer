package red.felnull.imp.music.info.tracker;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import red.felnull.imp.IamMusicPlayer;

public class FixedMusicTracker extends MusicTracker {
    private Vec3 position;

    public FixedMusicTracker(Vec3 position) {
        this.position = position;
    }

    @Override
    public ResourceLocation getTrackerDefinition() {
        return new ResourceLocation(IamMusicPlayer.MODID, "fixed");
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putDouble("PositionX", position.x);
        tag.putDouble("PositionY", position.y);
        tag.putDouble("PositionZ", position.z);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        this.position = new Vec3(tag.getDouble("PositionX"), tag.getDouble("PositionY"), tag.getDouble("PositionZ"));
    }
}
