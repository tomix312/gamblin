package pl.kasyno;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class KasynoCommand implements CommandExecutor {

    public static final String KASYNO_TITLE = "§6§l✦ KASYNO ✦ §7(50/50)";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cTa komenda jest tylko dla graczy!");
            return true;
        }

        Player player = (Player) sender;
        
        // Tworzenie GUI 27 slotow (3 rzedy)
        Inventory gui = Bukkit.createInventory(null, 27, KASYNO_TITLE);
        
        // Wypelnienie szarymi szybkami (dekoracja)
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, glass);
        }
        
        // Slot na przedmiot gracza (srodek - slot 13)
        gui.setItem(13, null);
        
        // Przycisk GRAJ (zielony beton)
        ItemStack playButton = createItem(Material.LIME_CONCRETE, 
            "§a§lGRAJ!", 
            "§7Kliknij aby zagrac!",
            "§7",
            "§e50% szans §7na §a§lPODWOJENIE",
            "§e50% szans §7na §c§lUTRATE",
            "§7",
            "§8Wloz przedmiot w srodkowy slot!"
        );
        gui.setItem(11, playButton);
        
        // Przycisk ZAMKNIJ (czerwony beton)
        ItemStack closeButton = createItem(Material.RED_CONCRETE, 
            "§c§lZAMKNIJ",
            "§7Kliknij aby zamknac"
        );
        gui.setItem(15, closeButton);
        
        // Informacje (slot gora-srodek)
        ItemStack info = createItem(Material.GOLD_INGOT,
            "§6§lJAK GRAC?",
            "§7",
            "§f1. §7Wloz przedmiot w §esrodkowy slot",
            "§f2. §7Kliknij §aGRAJ!",
            "§f3. §7Modl sie o szczescie!",
            "§7",
            "§c⚠ Mozesz stracic przedmiot!"
        );
        gui.setItem(4, info);
        
        // Otworz GUI
        player.openInventory(gui);
        player.sendMessage("§6[Kasyno] §eOtwarto kasyno! Powodzenia!");
        
        return true;
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
}
