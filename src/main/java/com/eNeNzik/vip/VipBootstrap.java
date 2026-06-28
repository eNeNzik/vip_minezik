package com.eNeNzik.vip;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.DialogTagKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collection;
import java.util.List;

public final class VipBootstrap implements PluginBootstrap {

    private static final TypedKey<Dialog> MINEZIK_PAUSE_DIALOG_KEY =
            RegistryKey.DIALOG.typedKey(Key.key("vip", "minezik_pause"));

    @Override
    public void bootstrap(BootstrapContext context) {
        registerCommand(context);
        registerDialogs(context);
    }

    private void registerCommand(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                event.registrar().register(
                        context.getConfiguration(),
                        "vip",
                        "Open Minezik VIP menu",
                        List.of(),
                        new BasicCommand() {
                            @Override
                            public void execute(CommandSourceStack source, String[] args) {
                                Vip.dispatchVipCommand(source.getSender(), args);
                            }

                            @Override
                            public Collection<String> suggest(CommandSourceStack source, String[] args) {
                                return Vip.completeVipCommand(source.getSender(), args);
                            }
                        }
                );
                event.registrar().register(
                        context.getConfiguration(),
                        "chattogle",
                        "Toggle default chat mode",
                        List.of("chattoggle"),
                        new BasicCommand() {
                            @Override
                            public void execute(CommandSourceStack source, String[] args) {
                                Vip.toggleChatMode(source.getSender());
                            }
                        }
                );
        });
    }

    private void registerDialogs(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(RegistryEvents.DIALOG.compose(), event -> {
            registerMinezikMenuDialog(event.registry(), MINEZIK_PAUSE_DIALOG_KEY);
        });

        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.postFlatten(RegistryKey.DIALOG),
                event -> {
                    event.registrar().addToTag(DialogTagKeys.PAUSE_SCREEN_ADDITIONS, List.of(MINEZIK_PAUSE_DIALOG_KEY));
                    event.registrar().addToTag(DialogTagKeys.QUICK_ACTIONS, List.of(MINEZIK_PAUSE_DIALOG_KEY));
                }
        );
    }

    private void registerMinezikMenuDialog(
            io.papermc.paper.registry.event.WritableRegistry<Dialog, io.papermc.paper.registry.data.dialog.DialogRegistryEntry.Builder> registry,
            TypedKey<Dialog> key
    ) {
        registry.register(key, builder -> builder
                .base(DialogBase.builder(minezikLogo())
                        .externalTitle(minezikLogo())
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(
                                minezikMenuText(),
                                260
                        )))
                        .build())
                .type(DialogType.multiAction(List.of(
                                commandButton("VIP menu", NamedTextColor.GREEN, "/vip"),
                                commandButton("Rules / Правила", NamedTextColor.YELLOW, "/rules")
                        ))
                        .exitAction(ActionButton.builder(Component.text("X", NamedTextColor.RED))
                                .width(40)
                                .action(DialogAction.staticAction(ClickEvent.runCommand("/vip close_dialog")))
                                .build())
                        .columns(1)
                        .build())
        );
    }

    private ActionButton commandButton(String label, NamedTextColor color, String command) {
        return ActionButton.builder(Component.text(label, color))
                .width(220)
                .action(DialogAction.staticAction(ClickEvent.runCommand(command)))
                .build();
    }

    private Component minezikLogo() {
        return Component.text("M", NamedTextColor.RED)
                .append(Component.text("i", NamedTextColor.GOLD))
                .append(Component.text("n", NamedTextColor.YELLOW))
                .append(Component.text("e", NamedTextColor.GREEN))
                .append(Component.text("z", NamedTextColor.BLUE))
                .append(Component.text("i", NamedTextColor.DARK_BLUE))
                .append(Component.text("k", NamedTextColor.DARK_PURPLE));
    }

    private Component minezikMenuText() {
        return minezikLogo()
                .append(Component.text(" menu", NamedTextColor.YELLOW));
    }
}
