import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FastNginx {

    // Green Sci-Fi Color Codes
    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[32m";
    private static final String BRIGHT_GREEN = "\033[92m";
    private static final String CYAN = "\033[36m";
    private static final String BRIGHT_CYAN = "\033[96m";
    private static final String DIM_GREEN = "\033[2;32m";
    private static final String RED = "\033[31m";
    private static final String YELLOW = "\033[33m";
    private static final String BOLD = "\033[1m";

    private static final String CONFIG_FILE = ".fastnginx_config";
    private static Scanner scanner = new Scanner(System.in);

    public static void clearScreen() {
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }

    private static void printSciFiHeader() {
        System.out.println(BRIGHT_GREEN + "╔══════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(BRIGHT_GREEN + "║" + CYAN
                + "    ▄▀█ █▀▀ ▄▀█ █▀ ▀█▀   █▄░█ █▀▀ █ █▄░█ ▀▄ ▄▀   ▀▀█▀▀ █▀█ █▀█ █░░   " + BRIGHT_GREEN + "║" + RESET);
        System.out.println(BRIGHT_GREEN + "║" + CYAN
                + "    █▀▀ █▄▄ █▀█ ▄█ ░█░   █░▀█ █▄█ █ █░▀█ ░▀▄▀░   ░░█░░ █▄█ █▄█ █▄▄   " + BRIGHT_GREEN + "║" + RESET);
        System.out.println(BRIGHT_GREEN + "║" + DIM_GREEN
                + "                    [ NEURAL NETWORK PROXY MANAGER ]                  " + BRIGHT_GREEN + "║"
                + RESET);
        System.out.println(BRIGHT_GREEN + "╚══════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    private static void printStatus(String message, String status) {
        String statusIcon = switch (status.toLowerCase()) {
            case "success", "ok" -> BRIGHT_GREEN + "✓";
            case "error", "fail" -> RED + "✗";
            case "warning" -> YELLOW + "⚠";
            case "info" -> CYAN + "ℹ";
            case "loading" -> BRIGHT_CYAN + "⟳";
            default -> CYAN + "→";
        };
        System.out.println(statusIcon + " " + GREEN + message + RESET);
    }

    private static String getUserInput(String prompt) {
        System.out.print(BRIGHT_CYAN + "┌─[" + GREEN + prompt + BRIGHT_CYAN + "]" + RESET);
        System.out.print(BRIGHT_CYAN + "\n└──➤ " + RESET);
        return scanner.nextLine().trim();
    }

    private static void showProgressBar(String task) {
        System.out.print(CYAN + task + ": " + RESET);
        for (int i = 0; i <= 20; i++) {
            System.out.print(BRIGHT_GREEN + "█" + RESET);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println(" " + BRIGHT_GREEN + "COMPLETE" + RESET);
    }

    public static void initializeSystem() {
        File configFile = new File(CONFIG_FILE);

        if (!configFile.exists()) {
            printStatus("Configuration file not detected", "warning");
            String basePath = getUserInput("Initialize system path");

            if (basePath.isEmpty()) {
                printStatus("Invalid path. System initialization failed", "error");
                System.exit(1);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath())) {
                writer.write(basePath);
                writer.newLine();
                printStatus("System configuration saved", "success");
            } catch (IOException e) {
                printStatus("Failed to save configuration: " + e.getMessage(), "error");
                return;
            }
        }

        try {
            List<String> lines = Files.readAllLines(configFile.toPath());
            if (lines.isEmpty()) {
                printStatus("Configuration file corrupted", "error");
                return;
            }

            String storedPath = lines.get(0).trim();
            Path basePath = Paths.get(storedPath);

            if (!Files.exists(basePath)) {
                printStatus("System path not found: " + storedPath, "error");
                return;
            }

            // Initialize directory structure
            Path dataDir = basePath.resolve("nginx_data");
            Path configIndex = dataDir.resolve("config_index");

            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                printStatus("Data directory created", "info");
            }

            if (!Files.exists(configIndex)) {
                Files.createFile(configIndex);
                printStatus("Configuration index initialized", "info");
            }

            printStatus("System ready for operation", "success");

        } catch (IOException e) {
            printStatus("System initialization error: " + e.getMessage(), "error");
        }
    }

    private static void deployProxyConfig() {
        try {
            showProgressBar("Scanning system parameters");

            String basePath = Files.readAllLines(Paths.get(CONFIG_FILE)).get(0).trim();
            Path dataDir = Paths.get(basePath, "nginx_data");
            Path configIndex = dataDir.resolve("config_index");

            System.out.println(BRIGHT_GREEN + "\n┌─── PROXY CONFIGURATION MATRIX ───┐" + RESET);

            String serviceType = getUserInput("Service Protocol [proxy/static]");
            if (!serviceType.equalsIgnoreCase("proxy")) {
                printStatus("Only proxy protocol supported in current build", "error");
                return;
            }

            String domain = getUserInput("Target Domain");
            if (domain.isEmpty()) {
                printStatus("Domain parameter required", "error");
                return;
            }

            String port = getUserInput("Backend Port");
            if (port.isEmpty() || !port.matches("\\d+")) {
                printStatus("Valid port number required", "error");
                return;
            }

            String ipAddress = "127.0.0.1"; // Default

            showProgressBar("Generating nginx configuration");

            String nginxConfig = generateNginxConfig(domain, port);

            // Write nginx configuration
            Path siteAvailable = Paths.get("/etc/nginx/sites-available", domain);
            Files.writeString(siteAvailable, nginxConfig);

            // Create symbolic link
            Path siteEnabled = Paths.get("/etc/nginx/sites-enabled", domain);
            if (!Files.exists(siteEnabled)) {
                Files.createSymbolicLink(siteEnabled, siteAvailable);
            }

            printStatus("Configuration deployed to nginx", "info");

            // Test nginx configuration
            showProgressBar("Running system diagnostics");
            Process testProcess = new ProcessBuilder("sudo", "nginx", "-t")
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start();

            int testResult = testProcess.waitFor();

            if (testResult == 0) {
                printStatus("Configuration validation PASSED", "success");

                // Reload nginx
                showProgressBar("Reloading nginx service");
                Process reloadProcess = new ProcessBuilder("sudo", "systemctl", "reload", "nginx")
                        .start();
                reloadProcess.waitFor();

                printStatus("Nginx service reloaded successfully", "success");

                // Ask about hosts file
                String addToHosts = getUserInput("Add domain to /etc/hosts? [y/N]");
                if (addToHosts.equalsIgnoreCase("y")) {
                    String customIp = getUserInput("IP Address [127.0.0.1]");
                    if (!customIp.isEmpty()) {
                        ipAddress = customIp;
                    }

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter("/etc/hosts", true))) {
                        writer.newLine();
                        writer.write(ipAddress + "\t" + domain + "\t# Added by FastNginx");
                        writer.newLine();
                        printStatus("Domain added to hosts file", "success");
                    }
                }

                // Save to index
                String configEntry = String.format(
                        "domain=%s,port=%s,type=proxy,ip=%s,path=%s,status=active,created=%d",
                        domain, port, ipAddress, siteAvailable.toString(), System.currentTimeMillis());

                try (BufferedWriter writer = Files.newBufferedWriter(configIndex, StandardOpenOption.APPEND)) {
                    writer.write(configEntry);
                    writer.newLine();
                }

                printStatus("Configuration registered in system index", "success");
                System.out.println(BRIGHT_GREEN + "\n[ DEPLOYMENT COMPLETE - SYSTEM OPERATIONAL ]" + RESET);

            } else {
                printStatus("Configuration validation FAILED", "error");

                // Read error output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(testProcess.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(RED + "  " + line + RESET);
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            printStatus("Deployment failed: " + e.getMessage(), "error");
        }
    }

    private static String generateNginxConfig(String domain, String port) {
        return String.format("""
                # FastNginx Generated Configuration
                # Domain: %s | Port: %s | Generated: %s

                server {
                    listen 80;
                    server_name %s;

                    # Security headers
                    add_header X-Frame-Options "SAMEORIGIN" always;
                    add_header X-Content-Type-Options "nosniff" always;
                    add_header X-XSS-Protection "1; mode=block" always;

                    # Proxy configuration
                    location / {
                        proxy_pass http://127.0.0.1:%s;
                        proxy_http_version 1.1;

                        # WebSocket support
                        proxy_set_header Upgrade $http_upgrade;
                        proxy_set_header Connection 'upgrade';
                        proxy_cache_bypass $http_upgrade;

                        # Standard proxy headers
                        proxy_set_header Host $host;
                        proxy_set_header X-Real-IP $remote_addr;
                        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                        proxy_set_header X-Forwarded-Proto $scheme;

                        # Timeouts
                        proxy_connect_timeout 60s;
                        proxy_send_timeout 60s;
                        proxy_read_timeout 60s;
                    }

                    # Health check endpoint
                    location /nginx-health {
                        access_log off;
                        return 200 "healthy\\n";
                        add_header Content-Type text/plain;
                    }
                }
                """,
                domain, port, new Date().toString(), domain, port);
    }

    private static void manageConfigurations() {
        try {
            String basePath = Files.readAllLines(Paths.get(CONFIG_FILE)).get(0).trim();
            Path configIndex = Paths.get(basePath, "nginx_data", "config_index");

            if (!Files.exists(configIndex)) {
                printStatus("No configurations found", "info");
                return;
            }

            List<String> configurations = Files.readAllLines(configIndex);
            if (configurations.isEmpty()) {
                printStatus("Configuration database is empty", "info");
                return;
            }

            System.out.println(BRIGHT_GREEN + "\n╔═══ CONFIGURATION MATRIX ═══╗" + RESET);

            for (int i = 0; i < configurations.size(); i++) {
                String config = configurations.get(i);
                Map<String, String> configMap = parseConfigLine(config);

                String status = configMap.getOrDefault("status", "unknown");
                String statusIcon = status.equals("active") ? BRIGHT_GREEN + "●" : RED + "●";

                System.out.printf(CYAN + "[%02d] " + RESET + "%s " + GREEN + "%-20s" + RESET +
                        DIM_GREEN + " → " + RESET + ":%s" + DIM_GREEN + " (%s)" + RESET + "%n",
                        i + 1, statusIcon, configMap.get("domain"), configMap.get("port"),
                        configMap.get("type"));
            }

            System.out.println(BRIGHT_GREEN + "╚═══════════════════════════╝" + RESET);

            String selection = getUserInput(
                    "Select configuration [1-" + configurations.size() + "] or ENTER to cancel");
            if (selection.isEmpty())
                return;

            try {
                int index = Integer.parseInt(selection) - 1;
                if (index < 0 || index >= configurations.size()) {
                    printStatus("Invalid selection", "error");
                    return;
                }

                String configLine = configurations.get(index);
                Map<String, String> config = parseConfigLine(configLine);

                System.out.println(BRIGHT_CYAN + "\n┌─── CONFIGURATION DETAILS ───┐" + RESET);
                config.forEach((key, value) -> System.out.println(GREEN + "  " + key + ": " + RESET + value));
                System.out.println(BRIGHT_CYAN + "└─────────────────────────────┘" + RESET);

                String action = getUserInput("Action: [E]dit / [D]elete / [T]oggle / [ENTER] cancel");

                switch (action.toLowerCase()) {
                    case "e" -> editConfiguration(configurations, index, configIndex);
                    case "d" -> deleteConfiguration(configurations, index, config, configIndex);
                    case "t" -> toggleConfiguration(configurations, index, config, configIndex);
                    default -> printStatus("Operation cancelled", "info");
                }

            } catch (NumberFormatException e) {
                printStatus("Invalid input format", "error");
            }

        } catch (IOException e) {
            printStatus("Error accessing configurations: " + e.getMessage(), "error");
        }
    }

    private static Map<String, String> parseConfigLine(String line) {
        Map<String, String> result = new HashMap<>();
        String[] parts = line.split(",");

        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                result.put(kv[0].trim(), kv[1].trim());
            }
        }
        return result;
    }

    private static void editConfiguration(List<String> configs, int index, Path configIndex) {
    try {
        Map<String, String> config = parseConfigLine(configs.get(index));
        String oldDomain = config.get("domain");
        String oldPort = config.get("port");
        String oldIp = config.get("ip");
        
        // รับข้อมูลใหม่จากผู้ใช้
        String newDomain = getUserInput("New domain [" + oldDomain + "]").trim();
        String newPort = getUserInput("New port [" + oldPort + "]").trim();
        String newIp = getUserInput("New IP [" + oldIp + "]").trim();

        // ใช้ค่าเดิมถ้าไม่ได้กรอกใหม่
        if (newDomain.isEmpty()) newDomain = oldDomain;
        if (newPort.isEmpty()) newPort = oldPort;
        if (newIp.isEmpty()) newIp = oldIp;

        // อัปเดตค่าใน config
        config.put("domain", newDomain);
        config.put("port", newPort);
        config.put("ip", newIp);

        Path oldPath = Paths.get(config.get("path"));
        Path newPath = oldPath;
        boolean domainChanged = !oldDomain.equals(newDomain);

        if (domainChanged) {
            // เปลี่ยนชื่อไฟล์ให้ตรงกับ domain ใหม่
            String oldFilename = oldPath.getFileName().toString();
            String newFilename = newDomain; // ใช้ domain ใหม่เป็นชื่อไฟล์
            Path availableDir = oldPath.getParent(); // /etc/nginx/sites-available
            newPath = availableDir.resolve(newFilename);

            // เปลี่ยน path ใหม่ใน config
            config.put("path", newPath.toString());

            // จัดการ symlink ใน sites-enabled ก่อน
            Path enabledDir = availableDir.getParent().resolve("sites-enabled");
            Path oldLink = enabledDir.resolve(oldFilename);
            Path newLink = enabledDir.resolve(newFilename);

            // ลบ symlink เดิมก่อน (ถ้ามี)
            if (Files.exists(oldLink)) {
                try {
                    Files.delete(oldLink);
                    printStatus("Deleted old symlink: " + oldFilename, "info");
                } catch (IOException e) {
                    printStatus("Warning: Could not delete old symlink " + oldFilename + ": " + e.getMessage(), "warning");
                }
            }

            // เปลี่ยนชื่อไฟล์จริงใน sites-available
            if (Files.exists(oldPath)) {
                Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                printStatus("Moved config file: " + oldFilename + " -> " + newFilename, "info");
            }

            // สร้าง symlink ใหม่
            try {
                Files.createSymbolicLink(newLink, newPath);
                printStatus("Created new symlink: " + newFilename, "success");
            } catch (IOException e) {
                printStatus("Warning: Could not create new symlink: " + e.getMessage(), "warning");
                // ลองใช้วิธีอื่น เช่น ln -s command
                try {
                    Process linkProcess = new ProcessBuilder("sudo", "ln", "-sf", newPath.toString(), newLink.toString()).start();
                    int exitCode = linkProcess.waitFor();
                    if (exitCode == 0) {
                        printStatus("Created symlink using ln command", "success");
                    } else {
                        printStatus("Failed to create symlink with ln command", "error");
                    }
                } catch (Exception ex) {
                    printStatus("Failed to create symlink with ln command: " + ex.getMessage(), "error");
                }
            }
        }

        // เขียนเนื้อหา nginx config ใหม่ลงไฟล์
        String nginxConfig = generateNginxConfig(newDomain, newPort);
        Files.writeString(newPath, nginxConfig);
        printStatus("Updated nginx configuration", "success");

        // อัปเดต config line ใน configIndex
        String updatedLine = config.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
        configs.set(index, updatedLine);
        Files.write(configIndex, configs);
        printStatus("Updated configuration index", "success");

        // อัปเดต /etc/hosts ให้ตรงกับการเปลี่ยนแปลง
        updateHostsFile(oldDomain, newDomain, newIp);

        // Test และ Reload nginx
        printStatus("Testing nginx configuration...", "loading");
        Process testProcess = new ProcessBuilder("sudo", "nginx", "-t")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start();

        int testResult = testProcess.waitFor();
        if (testResult == 0) {
            printStatus("Configuration test PASSED", "success");
            
            // Reload nginx
            Process nginxReload = new ProcessBuilder("sudo", "systemctl", "reload", "nginx").start();
            int reloadResult = nginxReload.waitFor();
            if (reloadResult == 0) {
                printStatus("Nginx reloaded successfully", "success");
                printStatus("Configuration updated successfully", "success");
            } else {
                printStatus("Warning: Nginx reload failed with exit code: " + reloadResult, "warning");
            }
        } else {
            printStatus("Configuration test FAILED", "error");
            // อ่าน error output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(testProcess.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(RED + "  " + line + RESET);
                }
            }
        }

    } catch (IOException | InterruptedException e) {
        printStatus("Failed to update configuration: " + e.getMessage(), "error");
    }
}

/**
 * อัปเดต /etc/hosts file ให้ตรงกับการเปลี่ยนแปลง
 */
private static void updateHostsFile(String oldDomain, String newDomain, String newIp) {
    try {
        Path hostsPath = Paths.get("/etc/hosts");
        List<String> hostsLines = Files.readAllLines(hostsPath);
        boolean foundAndUpdated = false;
        
        // หาและอัปเดตบรรทัดที่มี oldDomain
        for (int i = 0; i < hostsLines.size(); i++) {
            String line = hostsLines.get(i).trim();
            
            // ตรวจสอบบรรทัดที่มี comment "# Added by FastNginx" หรือมี domain เดิม
            if (line.contains(oldDomain) && 
                (line.contains("# Added by FastNginx") || line.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+\\s+" + oldDomain + ".*"))) {
                
                // สร้างบรรทัดใหม่
                String newLine = newIp + "\t" + newDomain + "\t# Added by FastNginx";
                hostsLines.set(i, newLine);
                foundAndUpdated = true;
                printStatus("Updated hosts entry: " + oldDomain + " -> " + newDomain, "info");
                break;
            }
        }
        
        // ถ้าไม่เจอบรรทัดเดิม ให้เพิ่มบรรทัดใหม่
        if (!foundAndUpdated) {
            // ตรวจสอบว่ามี domain ใหม่อยู่แล้วหรือไม่
            boolean newDomainExists = hostsLines.stream()
                    .anyMatch(line -> line.contains(newDomain));
            
            if (!newDomainExists) {
                hostsLines.add(newIp + "\t" + newDomain + "\t# Added by FastNginx");
                printStatus("Added new hosts entry: " + newDomain, "info");
            } else {
                printStatus("Domain " + newDomain + " already exists in hosts file", "warning");
            }
        }
        
        // เขียนกลับไปยังไฟล์
        Files.write(hostsPath, hostsLines);
        printStatus("Hosts file updated successfully", "success");
        
    } catch (IOException e) {
        printStatus("Failed to update hosts file: " + e.getMessage(), "error");
        
        // ถามผู้ใช้ว่าต้องการเพิ่มด้วยตนเองหรือไม่
        String manualAdd = getUserInput("Add to hosts manually? [y/N]").trim().toLowerCase();
        if (manualAdd.equals("y")) {
            System.out.println(CYAN + "Please add this line to /etc/hosts:" + RESET);
            System.out.println(BRIGHT_GREEN + newIp + "\t" + newDomain + "\t# Added by FastNginx" + RESET);
        }
    }
}

    private static void deleteConfiguration(List<String> configs, int index, Map<String, String> config,
            Path configIndex) {
        try {
            String domain = config.get("domain");
            String path = config.get("path");

            // Remove nginx files
            Files.deleteIfExists(Paths.get(path));
            Files.deleteIfExists(Paths.get("/etc/nginx/sites-enabled", domain));

            // Remove from hosts file
            List<String> hostsLines = Files.readAllLines(Paths.get("/etc/hosts"));
            List<String> updatedHosts = hostsLines.stream()
                    .filter(line -> !line.trim().endsWith("\t" + domain + "\t# Added by FastNginx"))
                    .collect(Collectors.toList());
            Files.write(Paths.get("/etc/hosts"), updatedHosts);

            // Remove from config index
            configs.remove(index);
            Files.write(configIndex, configs);

            // Reload nginx
            new ProcessBuilder("sudo", "systemctl", "reload", "nginx").start();

            printStatus("Configuration deleted successfully", "success");

        } catch (IOException e) {
            printStatus("Failed to delete configuration: " + e.getMessage(), "error");
        }
    }

    private static void toggleConfiguration(List<String> configs, int index, Map<String, String> config,
            Path configIndex) {
        try {
            String domain = config.get("domain");
            String currentStatus = config.getOrDefault("status", "active");
            String newStatus = currentStatus.equals("active") ? "inactive" : "active";

            Path enabledPath = Paths.get("/etc/nginx/sites-enabled", domain);
            Path availablePath = Paths.get(config.get("path"));

            if (newStatus.equals("active")) {
                if (!Files.exists(enabledPath)) {
                    Files.createSymbolicLink(enabledPath, availablePath);
                }
            } else {
                Files.deleteIfExists(enabledPath);
            }

            config.put("status", newStatus);
            String updatedLine = config.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(","));

            configs.set(index, updatedLine);
            Files.write(configIndex, configs);

            new ProcessBuilder("sudo", "systemctl", "reload", "nginx").start();

            printStatus("Configuration " + newStatus, "success");

        } catch (IOException e) {
            printStatus("Failed to toggle configuration: " + e.getMessage(), "error");
        }
    }

    private static void showMainMenu() {
        while (true) {
            clearScreen();
            printSciFiHeader();

            System.out.println(BRIGHT_GREEN + "╔═══════════════════════════════════════╗" + RESET);
            System.out.println(
                    BRIGHT_GREEN + "║" + CYAN + "  NEURAL COMMAND INTERFACE             " + BRIGHT_GREEN + "║" + RESET);
            System.out.println(BRIGHT_GREEN + "╠═══════════════════════════════════════╣" + RESET);
            System.out.println(BRIGHT_GREEN + "║" + GREEN + "  [1] Deploy Proxy Configuration       " + BRIGHT_GREEN
                    + "║" + RESET);
            System.out.println(BRIGHT_GREEN + "║" + GREEN + "  [2] Manage Configurations            " + BRIGHT_GREEN
                    + "║" + RESET);
            System.out.println(BRIGHT_GREEN + "║" + GREEN + "  [3] System Diagnostics               " + BRIGHT_GREEN
                    + "║" + RESET);
            System.out.println(
                    BRIGHT_GREEN + "║" + RED + "  [Q] Terminate Session                " + BRIGHT_GREEN + "║" + RESET);
            System.out.println(BRIGHT_GREEN + "╚═══════════════════════════════════════╝" + RESET);

            String choice = getUserInput("Command");

            switch (choice.toUpperCase()) {
                case "1" -> {
                    clearScreen();
                    deployProxyConfig();
                    getUserInput("Press ENTER to continue");
                }
                case "2" -> {
                    clearScreen();
                    manageConfigurations();
                    getUserInput("Press ENTER to continue");
                }
                case "3" -> {
                    clearScreen();
                    runDiagnostics();
                    getUserInput("Press ENTER to continue");
                }
                case "Q" -> {
                    printStatus("Neural network disconnected", "info");
                    System.exit(0);
                }
                default -> {
                    printStatus("Invalid command sequence", "error");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    private static void runDiagnostics() {
        System.out.println(BRIGHT_GREEN + "╔═══ SYSTEM DIAGNOSTICS ═══╗" + RESET);

        try {
            // Check nginx status
            Process nginxStatus = new ProcessBuilder("systemctl", "is-active", "nginx").start();
            int nginxResult = nginxStatus.waitFor();
            printStatus("Nginx Service: " + (nginxResult == 0 ? "ONLINE" : "OFFLINE"),
                    nginxResult == 0 ? "success" : "error");

            // Check configuration syntax
            Process configTest = new ProcessBuilder("sudo", "nginx", "-t").start();
            int configResult = configTest.waitFor();
            printStatus("Configuration Syntax: " + (configResult == 0 ? "VALID" : "INVALID"),
                    configResult == 0 ? "success" : "error");

            // Check ports
            printStatus("Scanning active ports...", "info");
            Process netstat = new ProcessBuilder("ss", "-tlnp").start();
            printStatus("Port scan completed", "success");

        } catch (IOException | InterruptedException e) {
            printStatus("Diagnostic scan failed: " + e.getMessage(), "error");
        }

        System.out.println(BRIGHT_GREEN + "╚═══════════════════════════╝" + RESET);
    }

    public static void main(String[] args) {
        try {
            clearScreen();
            showProgressBar("Initializing Neural Network");
            initializeSystem();
            Thread.sleep(1000); // Dramatic pause
            showMainMenu();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            printStatus("System interrupted", "error");
        }
    }
}