/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.woodandstonecraftingjournal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.journal.BrowserJournalChapterHandler;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalEntryProducer;
import org.terasology.journal.JournalManager;
import org.terasology.journal.TimestampResolver;
import org.terasology.journal.part.ImageJournalPart;
import org.terasology.journal.ui.ImageParagraph;
import org.terasology.journal.ui.RecipeParagraph;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.multiBlock.MultiBlockFormed;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.data.basic.HTMLLikeParser;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.utilities.Assets;
import org.terasology.workstationCrafting.component.CraftInHandIngredientComponent;
import org.terasology.workstationCrafting.component.CraftingStationComponent;
import org.terasology.workstationCrafting.component.CraftingStationIngredientComponent;
import org.terasology.workstationCrafting.component.CraftingStationToolComponent;
import org.terasology.workstationCrafting.event.CraftingStationUpgraded;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RegisterSystem
public class WoodCraftingJournalIntegration extends BaseComponentSystem {
    @In
    private JournalManager journalManager;
    @In
    private PrefabManager prefabManager;
    @In
    private BlockManager blockManager;

    private static final Logger logger = LoggerFactory.getLogger(WoodCraftingJournalIntegration.class);
    private String wasChapterId = "WoodAndStoneCrafting";
    private String seasonsChapterId = "Seasons";

    private ParagraphRenderStyle centerRenderStyle = new ParagraphRenderStyle() {
        @Override
        public HorizontalAlign getHorizontalAlignment() {
            return HorizontalAlign.CENTER;
        }
    };

    @Override
    public void preBegin() {

        BrowserJournalChapterHandler chapterHandler = new BrowserJournalChapterHandler();

        Prefab stoneItem = prefabManager.getPrefab("WoodCrafting:Stone");
        Prefab toolStoneItem = prefabManager.getPrefab("WoodCrafting:ToolStone");
        Prefab axeHammerHeadItem = prefabManager.getPrefab("WoodCrafting:AxeHammerHead");
        Prefab stickItem = prefabManager.getPrefab("WoodCrafting:Stick");
        Prefab twigItem = prefabManager.getPrefab("WoodCrafting:Twig");
        Prefab resinItem = prefabManager.getPrefab("WoodCrafting:Resin");
        Prefab unlitTorchItem = prefabManager.getPrefab("WoodCrafting:UnlitTorch");
        Prefab flintItem = prefabManager.getPrefab("WoodCrafting:Flint");

        Prefab crudeAxeHammerItem = prefabManager.getPrefab("WoodCrafting:CrudeAxeHammer");
        Prefab stoneHammerItem = prefabManager.getPrefab("StoneCrafting:StoneHammer");

        Block litTorchBlock = blockManager.getBlockFamily("WoodCrafting:LitTorch").getArchetypeBlock();

        chapterHandler.registerJournalEntry("Stone",
                Arrays.asList(
                        createTitleParagraph("Wood and Stone"),
                        createSubTitleParagraph("Stones"),
                        createTextParagraph("Stones are essential for making most tools. Digging the ground might be a good way to find stones." +
                                "Once I get two stones, I should be able to make a Tool Stone."),
                        new ImageParagraph(new Prefab[]{stoneItem}, null)
                ));

        chapterHandler.registerJournalEntry("ToolStone",
                Arrays.asList(
                        createSubTitleParagraph("Tool Stone"),
                        createTextParagraph("Now that I have stones, I can use two stones to make a Tool Stone which will allow me to build other equipment like axe " +
                                "(press N to open crafting window)."),
                        new ImageParagraph(new Prefab[]{toolStoneItem}, null),
                        createTextParagraph("By using the Tool Stone on another stone I should be able to make an Axe-Hammer Head by using the tool stone on another stone"),
                        new RecipeParagraph(new Block[2], new Prefab[]{toolStoneItem, stoneItem}, null, axeHammerHeadItem, 1)
                ));

        chapterHandler.registerJournalEntry("AxeHammer",
                Arrays.asList(
                        createSubTitleParagraph("Axe-Hammer"),
                        createTextParagraph("Now that I have an axe-hammer head, all I need is a stick for the handle and a twig to tie it together. " +
                                "I should be able to find these in the branches of trees."),
                        new ImageParagraph(new Prefab[]{stickItem, twigItem}, null),
                        createTextParagraph("Then I can combine the Axe-Hammer Head with a Stick and a Twig to create a Crude Axe-Hammer."),
                        new RecipeParagraph(new Block[3], new Prefab[]{axeHammerHeadItem, stickItem, twigItem}, null, crudeAxeHammerItem, 1)
                ));

        chapterHandler.registerJournalEntry("AxeHammerReceived",
                Arrays.asList(
                        createSubTitleParagraph("Axe-Hammer Created"),
                        createTextParagraph(("Excellent! I got the Axe-Hammer, I should be able to cut some of the trees with it. " +
                        "I can also use it to dig stone to get some more Stones for my crafting. It's not perfect but will have to do until I get my hands on a " +
                        "better hammer or a pick."))
                ));

        chapterHandler.registerJournalEntry("Logs",
                Arrays.asList(
                        createSubTitleParagraph("Logs"),
                        createTextParagraph(("These logs are big, there is no way I could handle them in my hands. " +
                        "I have to build a place where I could work on them. I should place two of the logs on the ground next to each other " +
                        "and then place my Axe on it (right-click your Axe-Hammer on the top face of one of the logs)."))
                ));

        chapterHandler.registerJournalEntry("WoodCraftingStation",
                Arrays.asList(
                        createSubTitleParagraph("Basic Wood Crafting Station"),
                        createTextParagraph(("Now I can work on the logs (to open the interface, press 'E' while pointing " +
                        "on the station).<l><l>I can store some ingredients in the left-top corner of the station and my axe in the bottom-center " +
                        "of the station. It's very crude and won't let me do much, but once I gather 10 Wood Planks I should be able to upgrade it. " +
                        "(to upgrade place the ingredients into lower-left corner of the interface and press the 'Upgrade' button)"))
                ));

        chapterHandler.registerJournalEntry("WoodCraftingStationUpgraded",
                Arrays.asList(
                        createSubTitleParagraph("Standard Wood Crafting Station"),
                        createTextParagraph(("Finally I can make something more useful than just planks. " +
                        "Not only that, but I can also create planks more efficiently! Quality of the workspace speaks for itself.<l><l>" +
                        "But I still can't make any tools. Hard to make it just out of wood, haha. I should probably find a good place " +
                        "to work on stone materials. I should make two tables using planks and sticks.<l><l>Once I get the tables I should place them " +
                        "on the ground next to each other and put my Axe-Hammer on top of one of them (same as before)."))
                ));

        chapterHandler.registerJournalEntry("StoneCraftingStation",
                new JournalEntryProducer() {
                    @Override
                    public Collection<ParagraphData> produceParagraph(long date) {
                        return Arrays.asList(
                                HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle, TimestampResolver.getJournalEntryDate(date)),
                                createSubTitleParagraph("Basic Stone Crafting Station"),
                                createTextParagraph("Now! On this workstation I should be able to create more durable tools. " +
                                        "I should get myself a couple of hammers and finally go mining!"),
                                new RecipeParagraph(new Block[3], new Prefab[]{stoneItem, twigItem, stickItem}, null, stoneHammerItem, 1),
                                createTextParagraph("It is going to be dark out there in the mines, I should prepare some torches in advance. " +
                                        "I can use some of the Resin found while cutting trees with stick in a crafting window (press G) to " +
                                        "create Unlit Torches."),
                                new RecipeParagraph(new Block[2], new Prefab[]{resinItem, stickItem}, null, unlitTorchItem, 1),
                                createTextParagraph("Once I get them I should be able to light them up using flint in a crafting window. " +
                                        "Just need to make sure not to light too many of them, as the torches last only for a bit of time."),
                                new RecipeParagraph(new Block[2], new Prefab[]{unlitTorchItem, flintItem}, litTorchBlock, null, 1)
                        );
                    }
                });

        journalManager.registerJournalChapter(wasChapterId,
                Assets.getTextureRegion("WoodCrafting:journalIcons#WoodCrafting").get(),
                "Wood and Stone", chapterHandler);
    }

    private JournalEntryProducer createTimestampEntryProducer(String text) {
        return new JournalEntryProducer() {
            @Override
            public Collection<ParagraphData> produceParagraph(long date) {
                return Arrays.asList(
                        HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle, TimestampResolver.getJournalEntryDate(date)),
                        HTMLLikeParser.parseHTMLLikeParagraph(null,
                                text));
            }
        };
    }

    private ParagraphData createTextParagraph(String text) {
        return HTMLLikeParser.parseHTMLLikeParagraph(null, text);
    }

    private ParagraphData createTitleParagraph(String title) {
        return HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle, "<f engine:title>" + title + "</f>");
    }

    private ParagraphData createSubTitleParagraph(String title) {
        return HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle, "<c 198>" + title + "</c>");
    }

    @ReceiveEvent
    public void playerSpawned(OnPlayerSpawnedEvent event, EntityRef player) {
        player.send(new DiscoveredNewJournalEntry(wasChapterId, "Stone"));
    }

    @ReceiveEvent
    public void craftingStationFormed(MultiBlockFormed craftingStationFormed, EntityRef station,
                                      CraftingStationComponent craftingStationComponent) {
        EntityRef character = craftingStationFormed.getInstigator();
        String workstationType = craftingStationComponent.type;
        if (workstationType.equalsIgnoreCase("WoodCrafting:BasicWoodCrafting")) {
            character.send(new DiscoveredNewJournalEntry(wasChapterId, "WoodCraftingStation"));
        } else if (workstationType.equalsIgnoreCase("WoodCrafting:BasicStoneCrafting")) {
            character.send(new DiscoveredNewJournalEntry(wasChapterId, "StoneCraftingStation"));
        }
    }

    @ReceiveEvent
    public void craftingStationUpgraded(CraftingStationUpgraded craftingStationUpgraded, EntityRef character) {
        if (craftingStationUpgraded.getCraftingStation().getComponent(CraftingStationComponent.class).type.equalsIgnoreCase("WoodCrafting:StandardWoodcrafting")) {
            character.send(new DiscoveredNewJournalEntry(wasChapterId, "WoodCraftingStationUpgraded"));
        }
    }

    @ReceiveEvent
    public void playerPickedUpItem(InventorySlotChangedEvent event, EntityRef character,
                                   CharacterComponent characterComponent) {
        logger.info("item pick up");
        CraftingStationToolComponent toolComponent = event.getNewItem().getComponent(CraftingStationToolComponent.class);
        CraftingStationIngredientComponent ingredientComponent = event.getNewItem().getComponent(CraftingStationIngredientComponent.class);
        CraftInHandIngredientComponent craftInHandIngredientComponent = event.getNewItem().getComponent(CraftInHandIngredientComponent.class);
        if (toolComponent != null) {
            List<String> toolTypes = toolComponent.type;
            if (toolTypes.contains("hammer") && toolTypes.contains("axe")) {
                character.send(new DiscoveredNewJournalEntry(wasChapterId, "AxeHammerReceived"));
            }
        }
        if (ingredientComponent != null) {
            String ingredientType = ingredientComponent.type;
            logger.info("ingredienttype: " + ingredientType);
            if (ingredientType.equalsIgnoreCase("WoodCrafting:wood")) {
                character.send(new DiscoveredNewJournalEntry(wasChapterId, "Logs"));
            }
        }
        if (craftInHandIngredientComponent != null) {
            String componentType = craftInHandIngredientComponent.componentType;
            if (componentType.equalsIgnoreCase("WoodCrafting:stone")) {
                character.send(new DiscoveredNewJournalEntry(wasChapterId, "ToolStone"));
            }
            if (componentType.equalsIgnoreCase("WoodCrafting:axeHammerHead")) {
                character.send(new DiscoveredNewJournalEntry(wasChapterId, "AxeHammer"));
            }
        }
    }

}
