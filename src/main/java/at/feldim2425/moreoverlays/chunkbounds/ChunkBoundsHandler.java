package at.feldim2425.moreoverlays.chunkbounds;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class ChunkBoundsHandler {

	public static final int REGION_SIZEX = 32;
	public static final int REGION_SIZEZ = 32;
	public static final int REGION_SIZEY_CUBIC = 32;

	private static RenderMode mode = RenderMode.NONE;

	private final List<String> regionInfo = new ArrayList<String>();
	private final boolean isCubicChunksLoaded;

	private int playerPrevRegionPosX = Integer.MIN_VALUE;
	private int playerPrevRegionPosY = Integer.MIN_VALUE;
	private int playerPrevRegionPosZ = Integer.MIN_VALUE;

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new ChunkBoundsHandler(ModList.get().isLoaded("cubicchunks")));
	}

	public ChunkBoundsHandler(boolean modLoaded) {
		isCubicChunksLoaded = modLoaded;
	}

	public static RenderMode getMode() {
		return mode;
	}

	public static void setMode(RenderMode mode) {
		ChunkBoundsHandler.mode = mode;
	}

	public static void toggleMode() {
		RenderMode[] modes = RenderMode.values();
		mode = modes[(mode.ordinal() + 1) % modes.length];
	}

	@SubscribeEvent
	public void renderWorldLastEvent(RenderWorldLastEvent event) {
		if (mode != RenderMode.NONE) {
			ChunkBoundsRenderer.renderOverlays();
		}
	}

	@SubscribeEvent
	public void onOverlayRender(RenderGameOverlayEvent.Post event) {
		if (regionInfo.isEmpty()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameSettings.showDebugInfo) {
			return;
		}
		int y = 0;
		for (String text : regionInfo) {
			mc.fontRenderer.drawStringWithShadow(event.getMatrixStack(), text, 10, y += 10, 0xFFFFFF);
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) {
			return;
		}
		if (ChunkBoundsHandler.getMode() != ChunkBoundsHandler.RenderMode.REGIONS) {
			regionInfo.clear();
			playerPrevRegionPosX = 0;
			playerPrevRegionPosY = 0;
			playerPrevRegionPosZ = 0;
			return;
		}
		final PlayerEntity player = Minecraft.getInstance().player;
		boolean updateInfo = regionInfo.isEmpty();

		int newRegionX;
		if(player.chunkCoordX < 0){
			newRegionX = (player.chunkCoordX+1) / REGION_SIZEX;
			newRegionX--;
		} else {
            newRegionX = player.chunkCoordX / REGION_SIZEX;
		}
		if (playerPrevRegionPosX != newRegionX) {
			playerPrevRegionPosX = newRegionX;
			updateInfo = true;
		}

		if(isCubicChunksLoaded) {
			int newRegionY = player.chunkCoordY / REGION_SIZEY_CUBIC;
			if(player.chunkCoordY < 0){
				newRegionY--;
			}
			if (playerPrevRegionPosY != newRegionY) {
				playerPrevRegionPosY = newRegionY;
				updateInfo = true;
			}
		}

		int newRegionZ;
		if(player.chunkCoordZ < 0){
			newRegionZ = (player.chunkCoordZ+1) / REGION_SIZEZ;
			newRegionZ--;
		} else {
			newRegionZ = player.chunkCoordZ / REGION_SIZEZ;
		}
		if (playerPrevRegionPosZ != newRegionZ) {
			playerPrevRegionPosZ = newRegionZ;
			updateInfo = true;
		}

		if (updateInfo) {
			regionInfo.clear();
			if (isCubicChunksLoaded) {
				regionInfo.add(String.format("region2d/%d.%d.2dr", playerPrevRegionPosX, playerPrevRegionPosZ));
				regionInfo.add(String.format("region3d/%d.%d.%d.3dr", playerPrevRegionPosX, playerPrevRegionPosY,
						playerPrevRegionPosZ));
			} else {
				regionInfo.add(String.format("region/r.%d.%d.mca", playerPrevRegionPosX, playerPrevRegionPosZ));
			}
		}
	}

	public enum RenderMode {
		NONE,
		CORNERS,
		GRID,
		REGIONS
	}
}
