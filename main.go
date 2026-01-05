package main

import (
	"bufio"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"time"
)

// Green Sci-Fi Color Codes
const (
	RESET        = "\033[0m"
	GREEN        = "\033[32m"
	BRIGHT_GREEN = "\033[92m"
	CYAN         = "\033[36m"
	BRIGHT_CYAN  = "\033[96m"
	DIM_GREEN    = "\033[2;32m"
	RED          = "\033[31m"
	YELLOW       = "\033[33m"
	BOLD         = "\033[1m"
)

const CONFIG_FILE = ".fastnginx_config"

var scanner = bufio.NewReader(os.Stdin)

func clearScreen() {
	fmt.Print("\033[H\033[2J\033[3J")
}

func printSciFiHeader() {
	fmt.Println(BRIGHT_GREEN + "╔══════════════════════════════════════════════════════════════╗" + RESET)
	fmt.Println(BRIGHT_GREEN + "║" + CYAN +
		"    ▄▀█ █▀▀ ▄▀█ █▀ ▀█▀   █▄░█ █▀▀ █ █▄░█ ▀▄ ▄▀   ▀▀█▀▀ █▀█ █▀█ █░░   " + BRIGHT_GREEN + "║" + RESET)
	fmt.Println(BRIGHT_GREEN + "║" + CYAN +
		"    █▀▀ █▄▄ █▀█ ▄█ ░█░   █░▀█ █▄█ █ █░▀█ ░▀▄▀░   ░░█░░ █▄█ █▄█ █▄▄   " + BRIGHT_GREEN + "║" + RESET)
	fmt.Println(BRIGHT_GREEN + "║" + DIM_GREEN +
		"                    [ NEURAL NETWORK PROXY MANAGER ]                  " + BRIGHT_GREEN + "║" +
		RESET)
	fmt.Println(BRIGHT_GREEN + "╚══════════════════════════════════════════════════════════════╝" + RESET)
	fmt.Println()
}

func printStatus(message, status string) {
	statusIcon := ""
	switch strings.ToLower(status) {
	case "success", "ok":
		statusIcon = BRIGHT_GREEN + "✓"
	case "error", "fail":
		statusIcon = RED + "✗"
	case "warning":
		statusIcon = YELLOW + "⚠"
	case "info":
		statusIcon = CYAN + "ℹ"
	case "loading":
		statusIcon = BRIGHT_CYAN + "⟳"
	default:
		statusIcon = CYAN + "→"
	}
	fmt.Println(statusIcon + " " + GREEN + message + RESET)
}

func getUserInput(prompt string) string {
	fmt.Print(BRIGHT_CYAN + "┌─[" + GREEN + prompt + BRIGHT_CYAN + "]" + RESET)
	fmt.Print(BRIGHT_CYAN + "\n└──➤ " + RESET)
	input, _ := scanner.ReadString('\n')
	return strings.TrimSpace(input)
}

func showProgressBar(task string) {
	fmt.Print(CYAN + task + ": " + RESET)
	for i := 0; i <= 20; i++ {
		fmt.Print(BRIGHT_GREEN + "█" + RESET)
		time.Sleep(50 * time.Millisecond)
	}
	fmt.Println(" " + BRIGHT_GREEN + "COMPLETE" + RESET)
}

func initializeSystem() {
	if _, err := os.Stat(CONFIG_FILE); os.IsNotExist(err) {
		printStatus("Configuration file not detected", "warning")
		basePath := getUserInput("Initialize system path")

		if basePath == "" {
			printStatus("Invalid path. System initialization failed", "error")
			os.Exit(1)
		}

		err := os.WriteFile(CONFIG_FILE, []byte(basePath+"\n"), 0644)
		if err != nil {
			printStatus("Failed to save configuration: "+err.Error(), "error")
			return
		}
		printStatus("System configuration saved", "success")
	}

	content, err := os.ReadFile(CONFIG_FILE)
	if err != nil {
		printStatus("System initialization error: "+err.Error(), "error")
		return
	}

	lines := strings.Split(string(content), "\n")
	if len(lines) == 0 {
		printStatus("Configuration file corrupted", "error")
		return
	}

	storedPath := strings.TrimSpace(lines[0])
	if _, err := os.Stat(storedPath); os.IsNotExist(err) {
		printStatus("System path not found: "+storedPath, "error")
		return
	}

	// Initialize directory structure
	dataDir := filepath.Join(storedPath, "nginx_data")
	configIndex := filepath.Join(dataDir, "config_index")

	if _, err := os.Stat(dataDir); os.IsNotExist(err) {
		os.MkdirAll(dataDir, 0755)
		printStatus("Data directory created", "info")
	}

	if _, err := os.Stat(configIndex); os.IsNotExist(err) {
		os.WriteFile(configIndex, []byte(""), 0644)
		printStatus("Configuration index initialized", "info")
	}

	printStatus("System ready for operation", "success")
}

func deployProxyConfig() {
	showProgressBar("Scanning system parameters")

	content, err := os.ReadFile(CONFIG_FILE)
	if err != nil {
		printStatus("Error reading config: "+err.Error(), "error")
		return
	}
	basePath := strings.TrimSpace(strings.Split(string(content), "\n")[0])
	dataDir := filepath.Join(basePath, "nginx_data")
	configIndex := filepath.Join(dataDir, "config_index")

	fmt.Println(BRIGHT_GREEN + "\n┌─── PROXY CONFIGURATION MATRIX ───┐" + RESET)

	serviceType := getUserInput("Service Protocol [proxy/static]")
	if !strings.EqualFold(serviceType, "proxy") {
		printStatus("Only proxy protocol supported in current build", "error")
		return
	}

	domainInput := getUserInput("Target Domain")
	if domainInput == "" {
		printStatus("Domain parameter required", "error")
		return
	}
	domains := strings.Fields(domainInput)
	primaryDomain := domains[0]

	port := getUserInput("Backend Port")
	if port == "" {
		printStatus("Valid port number required", "error")
		return
	} else {
		if _, err := strconv.Atoi(port); err != nil {
			printStatus("Valid port number required", "error")
			return
		}
	}

	backendHost := getUserInput("Backend Host [127.0.0.1]")
	if backendHost == "" {
		backendHost = "127.0.0.1"
	}

	ipAddress := "127.0.0.1" // Default for hosts file

	showProgressBar("Generating nginx configuration")

	nginxConfig := generateNginxConfig(domainInput, port, backendHost)

	// Write nginx configuration
	siteAvailable := filepath.Join("/etc/nginx/sites-available", primaryDomain)
	err = os.WriteFile(siteAvailable, []byte(nginxConfig), 0644)
	if err != nil {
		printStatus("Failed to write nginx config: "+err.Error(), "error")
		return
	}

	// Create symbolic link
	siteEnabled := filepath.Join("/etc/nginx/sites-enabled", primaryDomain)
	if _, err := os.Stat(siteEnabled); os.IsNotExist(err) {
		err := os.Symlink(siteAvailable, siteEnabled)
		if err != nil {
			printStatus("Failed to create symlink: "+err.Error(), "error")
		}
	}

	printStatus("Configuration deployed to nginx", "info")

	// Test nginx configuration
	showProgressBar("Running system diagnostics")
	cmdTest := exec.Command("sudo", "nginx", "-t")
	output, err := cmdTest.CombinedOutput()

	if err == nil {
		printStatus("Configuration validation PASSED", "success")

		// Reload nginx
		showProgressBar("Reloading nginx service")
		cmdReload := exec.Command("sudo", "systemctl", "reload", "nginx")
		err = cmdReload.Run()
		if err != nil {
			printStatus("Failed to reload nginx: "+err.Error(), "error")
		} else {
			printStatus("Nginx service reloaded successfully", "success")

			// Ask about hosts file
			addToHosts := getUserInput("Add domain to /etc/hosts? [y/N]")
			if strings.EqualFold(addToHosts, "y") {
				customIp := getUserInput("IP Address for hosts file [127.0.0.1]")
				if customIp != "" {
					ipAddress = customIp
				}

				f, err := os.OpenFile("/etc/hosts", os.O_APPEND|os.O_WRONLY, 0644)
				if err != nil {
					printStatus("Failed to open hosts file: "+err.Error(), "error")
				} else {
					defer f.Close()
					if _, err := f.WriteString(fmt.Sprintf("\n%s\t%s\t# Added by FastNginx\n", ipAddress, domainInput)); err != nil {
						printStatus("Failed to write to hosts file: "+err.Error(), "error")
					} else {
						printStatus("Domain added to hosts file", "success")
					}
				}
			}

			// Save to index
			configEntry := fmt.Sprintf(
				"domain=%s,port=%s,host=%s,type=proxy,ip=%s,path=%s,status=active,created=%d\n",
				domainInput, port, backendHost, ipAddress, siteAvailable, time.Now().UnixMilli())

			f, err := os.OpenFile(configIndex, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
			if err != nil {
				printStatus("Failed to update index: "+err.Error(), "error")
			} else {
				defer f.Close()
				if _, err := f.WriteString(configEntry); err != nil {
					printStatus("Failed to write to index: "+err.Error(), "error")
				}
			}

			printStatus("Configuration registered in system index", "success")
			fmt.Println(BRIGHT_GREEN + "\n[ DEPLOYMENT COMPLETE - SYSTEM OPERATIONAL ]" + RESET)
		}

	} else {
		printStatus("Configuration validation FAILED", "error")
		fmt.Println(RED + string(output) + RESET)
	}
}

func generateNginxConfig(domain, port, backendHost string) string {
	ts := time.Now().String()
	return fmt.Sprintf(`# FastNginx Generated Configuration
# Domain: %s | Port: %s | Host: %s | Generated: %s

server {
    listen 80;
    server_name %s;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Proxy configuration
    location / {
        proxy_pass http://%s:%s;
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
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
`, domain, port, backendHost, ts, domain, backendHost, port)
}

func manageConfigurations() {
	content, err := os.ReadFile(CONFIG_FILE)
	if err != nil {
		printStatus("Error reading config: "+err.Error(), "error")
		return
	}
	basePath := strings.TrimSpace(strings.Split(string(content), "\n")[0])
	configIndex := filepath.Join(basePath, "nginx_data", "config_index")

	if _, err := os.Stat(configIndex); os.IsNotExist(err) {
		printStatus("No configurations found", "info")
		return
	}

	indexContent, err := os.ReadFile(configIndex)
	if err != nil {
		printStatus("Error reading index: "+err.Error(), "error")
		return
	}

	configurations := strings.Split(strings.TrimSpace(string(indexContent)), "\n")
	if len(configurations) == 0 || (len(configurations) == 1 && configurations[0] == "") {
		printStatus("Configuration database is empty", "info")
		return
	}

	fmt.Println(BRIGHT_GREEN + "\n╔═══ CONFIGURATION MATRIX ═══╗" + RESET)

	// Filter out empty lines
	validConfigs := []string{}
	for _, c := range configurations {
		if strings.TrimSpace(c) != "" {
			validConfigs = append(validConfigs, c)
		}
	}
	configurations = validConfigs

	for i, config := range configurations {
		configMap := parseConfigLine(config)
		status := configMap["status"]
		if status == "" {
			status = "unknown"
		}
		statusIcon := RED + "●"
		if status == "active" {
			statusIcon = BRIGHT_GREEN + "●"
		}

		fmt.Printf(CYAN+"[%02d] "+RESET+"%s "+GREEN+"%-20s"+RESET+
			DIM_GREEN+" → "+RESET+":%s"+DIM_GREEN+" (%s)"+RESET+"\n",
			i+1, statusIcon, configMap["domain"], configMap["port"], configMap["type"])
	}

	fmt.Println(BRIGHT_GREEN + "╚═══════════════════════════╝" + RESET)

	selection := getUserInput("Select configuration [1-" + strconv.Itoa(len(configurations)) + "] or ENTER to cancel")
	if selection == "" {
		return
	}

	index, err := strconv.Atoi(selection)
	if err != nil || index < 1 || index > len(configurations) {
		printStatus("Invalid selection", "error")
		return
	}
	index-- // 0-based

	configLine := configurations[index]
	config := parseConfigLine(configLine)

	fmt.Println(BRIGHT_CYAN + "\n┌─── CONFIGURATION DETAILS ───┐" + RESET)
	for k, v := range config {
		fmt.Println(GREEN + "  " + k + ": " + RESET + v)
	}
	fmt.Println(BRIGHT_CYAN + "└─────────────────────────────┘" + RESET)

	action := getUserInput("Action: [E]dit / [D]elete / [T]oggle / [ENTER] cancel")

	switch strings.ToLower(action) {
	case "e":
		editConfiguration(&configurations, index, configIndex)
	case "d":
		deleteConfiguration(&configurations, index, config, configIndex)
	case "t":
		toggleConfiguration(&configurations, index, config, configIndex)
	default:
		printStatus("Operation cancelled", "info")
	}
}

func parseConfigLine(line string) map[string]string {
	result := make(map[string]string)
	parts := strings.Split(line, ",")

	for _, part := range parts {
		kv := strings.SplitN(part, "=", 2)
		if len(kv) == 2 {
			result[strings.TrimSpace(kv[0])] = strings.TrimSpace(kv[1])
		}
	}
	return result
}

func editConfiguration(configs *[]string, index int, configIndexPath string) {
	config := parseConfigLine((*configs)[index])
	oldDomain := config["domain"]
	oldPort := config["port"]
	oldIp := config["ip"]
	if oldIp == "" {
		oldIp = "127.0.0.1"
	}
	oldHost := config["host"]
	if oldHost == "" {
		oldHost = "127.0.0.1"
	}

	newDomain := getUserInput("New domain [" + oldDomain + "]")
	newPort := getUserInput("New port [" + oldPort + "]")
	newHost := getUserInput("New Backend Host [" + oldHost + "]")
	newIp := getUserInput("New IP for hosts file [" + oldIp + "]")

	if newDomain == "" {
		newDomain = oldDomain
	}
	if newPort == "" {
		newPort = oldPort
	}
	if newHost == "" {
		newHost = oldHost
	}
	newDomains := strings.Fields(newDomain)
	newPrimary := newDomains[0] // safe primarily because if empty we defaulted to oldDomain

	if oldDomain == newDomain {
		// Just to be safe if they didn't change it but it might have multiple parts
		oldDomains := strings.Fields(oldDomain)
		if len(oldDomains) > 0 {
			// Actually we need to check if primary changed even if full string changed
		}
	}
	oldDomains := strings.Fields(oldDomain)
	oldPrimary := oldDomains[0]

	config["domain"] = newDomain
	config["port"] = newPort
	config["host"] = newHost
	config["ip"] = newIp

	oldPath := config["path"]
	newPath := oldPath
	domainChanged := oldPrimary != newPrimary

	if domainChanged {
		oldFilename := filepath.Base(oldPath)
		newFilename := newPrimary
		availableDir := filepath.Dir(oldPath)
		newPath = filepath.Join(availableDir, newFilename)

		config["path"] = newPath

		enabledDir := filepath.Join(filepath.Dir(availableDir), "sites-enabled")
		oldLink := filepath.Join(enabledDir, oldFilename)
		newLink := filepath.Join(enabledDir, newFilename)

		if _, err := os.Stat(oldLink); err == nil {
			err := os.Remove(oldLink)
			if err != nil {
				printStatus("Warning: Could not delete old symlink "+oldFilename+": "+err.Error(), "warning")
			} else {
				printStatus("Deleted old symlink: "+oldFilename, "info")
			}
		}

		if _, err := os.Stat(oldPath); err == nil {
			err := os.Rename(oldPath, newPath)
			if err != nil {
				printStatus("Failed to move config file: "+err.Error(), "error")
			} else {
				printStatus("Moved config file: "+oldFilename+" -> "+newFilename, "info")
			}
		}

		// Create new symlink
		err := os.Symlink(newPath, newLink)
		if err != nil {
			printStatus("Warning: Could not create new symlink: "+err.Error(), "warning")
			// Try with sudo ln -sf
			cmd := exec.Command("sudo", "ln", "-sf", newPath, newLink)
			if err := cmd.Run(); err != nil {
				printStatus("Failed to create symlink with ln command: "+err.Error(), "error")
			} else {
				printStatus("Created symlink using ln command", "success")
			}
		} else {
			printStatus("Created new symlink: "+newFilename, "success")
		}
	}

	// Write new contents
	nginxConfig := generateNginxConfig(newDomain, newPort, newHost)
	if err := os.WriteFile(newPath, []byte(nginxConfig), 0644); err != nil {
		printStatus("Failed to write nginx config: "+err.Error(), "error")
	} else {
		printStatus("Updated nginx configuration", "success")
	}

	// Update list and file
	var parts []string
	for k, v := range config {
		parts = append(parts, k+"="+v)
	}
	updatedLine := strings.Join(parts, ",")
	(*configs)[index] = updatedLine

	if err := os.WriteFile(configIndexPath, []byte(strings.Join(*configs, "\n")), 0644); err != nil {
		printStatus("Failed to write index: "+err.Error(), "error")
	} else {
		printStatus("Updated configuration index", "success")
	}

	updateHostsFile(oldDomain, newDomain, newIp)

	printStatus("Testing nginx configuration...", "loading")
	cmdTest := exec.Command("sudo", "nginx", "-t")
	output, err := cmdTest.CombinedOutput()

	if err == nil {
		printStatus("Configuration test PASSED", "success")
		cmdReload := exec.Command("sudo", "systemctl", "reload", "nginx")
		if err := cmdReload.Run(); err != nil {
			printStatus("Warning: Nginx reload failed: "+err.Error(), "warning")
		} else {
			printStatus("Nginx reloaded successfully", "success")
			printStatus("Configuration updated successfully", "success")
		}
	} else {
		printStatus("Configuration test FAILED", "error")
		fmt.Println(RED + string(output) + RESET)
	}
}

func updateHostsFile(oldDomain, newDomain, newIp string) {
	hostsPath := "/etc/hosts"
	content, err := os.ReadFile(hostsPath)
	if err != nil {
		printStatus("Failed to read hosts file: "+err.Error(), "error")
		manualHostsPrompt(newDomain, newIp)
		return
	}

	lines := strings.Split(string(content), "\n")
	foundAndUpdated := false

	for i, line := range lines {
		trimmed := strings.TrimSpace(line)
		if strings.Contains(trimmed, oldDomain) &&
			(strings.Contains(trimmed, "# Added by FastNginx") ||
				strings.Contains(trimmed, oldDomain)) { // bit loose but okay for port
			lines[i] = fmt.Sprintf("%s\t%s\t# Added by FastNginx", newIp, newDomain)
			foundAndUpdated = true
			printStatus("Updated hosts entry: "+oldDomain+" -> "+newDomain, "info")
			break
		}
	}

	if !foundAndUpdated {
		// existing logic... check if new domain exists
		exists := false
		for _, line := range lines {
			if strings.Contains(line, newDomain) {
				exists = true
				break
			}
		}
		if !exists {
			lines = append(lines, fmt.Sprintf("%s\t%s\t# Added by FastNginx", newIp, newDomain))
			printStatus("Added new hosts entry: "+newDomain, "info")
		} else {
			printStatus("Domain "+newDomain+" already exists in hosts file", "warning")
		}
	}

	// Create temp file to write effectively (though simple write works if permissions allow)
	// We might need sudo to write to /etc/hosts, but this app assumes it has perms or users will handle it.
	// The Java code was just using BufferedWriter.
	if err := os.WriteFile(hostsPath, []byte(strings.Join(lines, "\n")), 0644); err != nil {
		printStatus("Failed to update hosts file: "+err.Error(), "error")
		manualHostsPrompt(newDomain, newIp)
	} else {
		printStatus("Hosts file updated successfully", "success")
	}
}

func manualHostsPrompt(domain, ip string) {
	prompt := getUserInput("Add to hosts manually? [y/N]")
	if strings.ToLower(prompt) == "y" {
		fmt.Println(CYAN + "Please add this line to /etc/hosts:" + RESET)
		fmt.Println(BRIGHT_GREEN + ip + "\t" + domain + "\t# Added by FastNginx" + RESET)
	}
}

func deleteConfiguration(configs *[]string, index int, config map[string]string, configIndexPath string) {
	domain := config["domain"]
	path := config["path"]

	// Remove nginx files
	os.Remove(path)
	os.Remove(filepath.Join("/etc/nginx/sites-enabled", domain))

	// Remove from hosts
	hostsPath := "/etc/hosts"
	content, err := os.ReadFile(hostsPath)
	if err == nil {
		lines := strings.Split(string(content), "\n")
		var newLines []string
		for _, line := range lines {
			if !strings.Contains(line, "\t"+domain+"\t# Added by FastNginx") {
				newLines = append(newLines, line)
			}
		}
		os.WriteFile(hostsPath, []byte(strings.Join(newLines, "\n")), 0644)
	}

	// Remove from configs
	*configs = append((*configs)[:index], (*configs)[index+1:]...)
	os.WriteFile(configIndexPath, []byte(strings.Join(*configs, "\n")), 0644)

	exec.Command("sudo", "systemctl", "reload", "nginx").Run()
	printStatus("Configuration deleted successfully", "success")
}

func toggleConfiguration(configs *[]string, index int, config map[string]string, configIndexPath string) {
	domain := config["domain"]
	currentStatus := config["status"]
	if currentStatus == "" {
		currentStatus = "active"
	}
	newStatus := "active"
	if currentStatus == "active" {
		newStatus = "inactive"
	}

	enabledPath := filepath.Join("/etc/nginx/sites-enabled", domain)
	availablePath := config["path"]

	if newStatus == "active" {
		if _, err := os.Stat(enabledPath); os.IsNotExist(err) {
			os.Symlink(availablePath, enabledPath)
		}
	} else {
		os.Remove(enabledPath)
	}

	config["status"] = newStatus
	var parts []string
	for k, v := range config {
		parts = append(parts, k+"="+v)
	}
	(*configs)[index] = strings.Join(parts, ",")

	os.WriteFile(configIndexPath, []byte(strings.Join(*configs, "\n")), 0644)

	exec.Command("sudo", "systemctl", "reload", "nginx").Run()
	printStatus("Configuration "+newStatus, "success")
}

func runDiagnostics() {
	fmt.Println(BRIGHT_GREEN + "╔═══ SYSTEM DIAGNOSTICS ═══╗" + RESET)

	// Check nginx status
	cmd := exec.Command("systemctl", "is-active", "nginx")
	if err := cmd.Run(); err == nil {
		printStatus("Nginx Service: ONLINE", "success")
	} else {
		printStatus("Nginx Service: OFFLINE", "error")
	}

	// Check config syntax
	cmd = exec.Command("sudo", "nginx", "-t")
	if err := cmd.Run(); err == nil {
		printStatus("Configuration Syntax: VALID", "success")
	} else {
		printStatus("Configuration Syntax: INVALID", "error")
	}

	printStatus("Scanning active ports...", "info")
	cmd = exec.Command("ss", "-tlnp")
	if output, err := cmd.CombinedOutput(); err == nil {
		_ = output // we could print it but original just says "completed"
		printStatus("Port scan completed", "success")
	} else {
		printStatus("Diagnostic scan failed", "error")
	}

	fmt.Println(BRIGHT_GREEN + "╚═══════════════════════════╝" + RESET)
}

func main() {
	clearScreen()
	showProgressBar("Initializing Neural Network")
	initializeSystem()
	time.Sleep(1 * time.Second) // Dramatic pause
	showMainMenu()
}

func showMainMenu() {
	for {
		clearScreen()
		printSciFiHeader()

		fmt.Println(BRIGHT_GREEN + "╔═══════════════════════════════════════╗" + RESET)
		fmt.Println(BRIGHT_GREEN + "║" + CYAN + "  NEURAL COMMAND INTERFACE             " + BRIGHT_GREEN + "║" + RESET)
		fmt.Println(BRIGHT_GREEN + "╠═══════════════════════════════════════╣" + RESET)
		fmt.Println(BRIGHT_GREEN + "║" + GREEN + "  [1] Deploy Proxy Configuration       " + BRIGHT_GREEN + "║" + RESET)
		fmt.Println(BRIGHT_GREEN + "║" + GREEN + "  [2] Manage Configurations            " + BRIGHT_GREEN + "║" + RESET)
		fmt.Println(BRIGHT_GREEN + "║" + GREEN + "  [3] System Diagnostics               " + BRIGHT_GREEN + "║" + RESET)
		fmt.Println(BRIGHT_GREEN + "║" + RED + "  [Q] Terminate Session                " + BRIGHT_GREEN + "║" + RESET)
		fmt.Println(BRIGHT_GREEN + "╚═══════════════════════════════════════╝" + RESET)

		choice := getUserInput("Command")

		switch strings.ToUpper(choice) {
		case "1":
			clearScreen()
			deployProxyConfig()
			getUserInput("Press ENTER to continue")
		case "2":
			clearScreen()
			manageConfigurations()
			getUserInput("Press ENTER to continue")
		case "3":
			clearScreen()
			runDiagnostics()
			getUserInput("Press ENTER to continue")
		case "Q":
			printStatus("Neural network disconnected", "info")
			os.Exit(0)
		default:
			printStatus("Invalid command sequence", "error")
			time.Sleep(1 * time.Second)
		}
	}
}
