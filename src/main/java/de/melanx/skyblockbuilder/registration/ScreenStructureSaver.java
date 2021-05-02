package de.melanx.skyblockbuilder.registration;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class ScreenStructureSaver extends Screen {

    private static final ResourceLocation SCREEN_LOCATION = new ResourceLocation(SkyblockBuilder.getInstance().modid, "textures/gui/structure_saver.png");

    private final int xSize;
    private final int ySize;
    private final ItemStack stack;
    @SuppressWarnings("FieldCanBeLocal")
    private int relX;
    @SuppressWarnings("FieldCanBeLocal")
    private int relY;
    private int xSave;
    private int ySave;
    private int xAbort;
    private int yAbort;
    private int xTrash;
    private int yTrash;
    private TextFieldWidget name;

    public ScreenStructureSaver(ItemStack stack, ITextComponent title) {
        super(title);
        this.xSize = 176;
        this.ySize = 85;
        this.stack = stack;
    }

    @Override
    public void init(@Nonnull Minecraft mc, int x, int y) {
        super.init(mc, x, y);
        int xButton = 55;
        this.relX = (x - this.xSize) / 2;
        this.relY = (y - this.ySize) / 2;
        this.xSave = this.relX + 10;
        this.ySave = this.relY + xButton;
        this.xAbort = this.relX + 77;
        this.yAbort = this.relY + xButton;
        this.xTrash = this.relX + 144;
        this.yTrash = this.relY + xButton;
        this.name = new TextFieldWidget(this.font, this.relX + 15, this.relY + 25, 143, 17, new StringTextComponent("test"));
        this.name.setMaxStringLength(32767);
        this.name.changeFocus(true);
        this.name.setText(this.name.getText());
    }

    @Override
    public void render(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        //noinspection ConstantConditions
        this.minecraft.getTextureManager().bindTexture(SCREEN_LOCATION);
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;
        this.blit(ms, relX, relY, 0, 0, this.xSize, this.ySize);
        Button button = this.getPressedButton(mouseX, mouseY);
        boolean overSave = button == Button.SAVE;
        boolean overAbort = button == Button.ABORT;
        boolean overTrash = button == Button.DELETE;

        this.blit(ms, this.xSave, this.ySave, 0, overSave ? this.ySize + 20 : this.ySize, 60, 20);
        this.blit(ms, this.xAbort, this.yAbort, 0, overAbort ? this.ySize + 20 : this.ySize, 60, 20);
        this.blit(ms, this.xTrash, this.yTrash, 60, overTrash ? this.ySize + 20 : this.ySize, 20, 20);
        this.name.render(ms, mouseX, mouseY, partialTicks);
        super.render(ms, mouseX, mouseY, partialTicks);
        this.font.func_243248_b(ms, this.title, this.relX + 10, this.relY + 8, Color.DARK_GRAY.getRGB());
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0) {
            Button pressed = this.getPressedButton((int) x, (int) y);
            if (pressed != Button.EMPTY) {
            this.closeScreen();
            RandomUtility.playSound(SoundEvents.UI_BUTTON_CLICK);
            SkyblockBuilder.getNetwork().handleButtonClick(this.stack, pressed, this.name.getText().isEmpty() ? "template" : this.name.getText());
        }}
        return super.mouseClicked(x, y, button);
    }

    @Nullable
    @Override
    public IGuiEventListener getListener() {
        return this.name;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputMappings.Input mapping = InputMappings.getInputByCode(keyCode, scanCode);
        //noinspection ConstantConditions
        if (keyCode != 256 && (this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mapping)
                || this.minecraft.gameSettings.keyBindDrop.isActiveAndMatches(mapping))) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        this.name.tick();
        super.tick();
    }

    public Button getPressedButton(int x, int y) {
        if (x >= this.xSave && x < this.xSave + 60 && y >= this.ySave && y < this.ySave + 20) {
            return Button.SAVE;
        } else if (x >= this.xAbort && x < this.xAbort + 60 && y >= this.yAbort && y < this.yAbort + 20) {
            return Button.ABORT;
        } else if (x >= this.xTrash && x < this.xTrash + 20 && y >= this.yTrash && y < this.yTrash + 20) {
            return Button.DELETE;
        }

        return Button.EMPTY;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public enum Button {
        EMPTY,
        SAVE,
        ABORT,
        DELETE;
    }
}