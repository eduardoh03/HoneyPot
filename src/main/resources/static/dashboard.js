// HoneyPot Security Dashboard - Integração com API
class SecurityDashboard {
    constructor() {
        this.apiUrl = '/api/honeypot';
        this.currentPage = 0;
        this.pageSize = 20;
        this.refreshInterval = 30000; // 30 segundos
        this.charts = {};
        this.isLoading = false;
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadInitialData();
        this.startAutoRefresh();
        this.showToast('Dashboard iniciado com sucesso!', 'success');
    }

    setupEventListeners() {
        // Controles do sistema
        document.getElementById('start-btn')?.addEventListener('click', () => this.startHoneypot());
        document.getElementById('stop-btn')?.addEventListener('click', () => this.stopHoneypot());
        document.getElementById('restart-btn')?.addEventListener('click', () => this.restartHoneypot());
        document.getElementById('refresh-btn')?.addEventListener('click', () => this.refreshAll());

        // Filtros de logs
        document.getElementById('protocol-filter')?.addEventListener('change', () => this.loadLogs());
        document.getElementById('ip-filter')?.addEventListener('input', this.debounce(() => this.loadLogs(), 500));
        document.getElementById('clear-logs-btn')?.addEventListener('click', () => this.clearLogs());

        // Paginação
        document.getElementById('prev-page')?.addEventListener('click', () => this.previousPage());
        document.getElementById('next-page')?.addEventListener('click', () => this.nextPage());
        
        // Notificações
        document.getElementById('mark-all-read-btn')?.addEventListener('click', () => this.markAllNotificationsAsRead());
        document.getElementById('view-all-notifications-btn')?.addEventListener('click', () => this.showAllNotificationsModal());
    }

    async loadInitialData() {
        this.setLoadingState(true);
        
        try {
            await Promise.all([
                this.loadStatus(),
                this.loadStats(),
                this.loadTopIps(),
                this.loadTopCredentials(),
                this.loadLogs(),
                this.loadNotifications(),
                this.initCharts()
            ]);
            
            this.updateLastUpdate();
        } catch (error) {
            console.error('Erro ao carregar dados iniciais:', error);
            this.showToast('Erro ao carregar dados do dashboard', 'error');
        } finally {
            this.setLoadingState(false);
        }
    }

    startAutoRefresh() {
        setInterval(() => {
            if (!this.isLoading) {
                this.refreshAll();
            }
        }, this.refreshInterval);
    }

    async refreshAll() {
        if (this.isLoading) return;
        
        try {
            await Promise.all([
                this.loadStatus(),
                this.loadStats(),
                this.loadTopIps(),
                this.loadTopCredentials(),
                this.loadLogs(),
                this.loadNotifications(),
                this.updateCharts()
            ]);
            
            this.updateLastUpdate();
        } catch (error) {
            console.error('Erro ao atualizar dashboard:', error);
        }
    }

    async apiCall(endpoint, options = {}) {
        try {
            const response = await fetch(`${this.apiUrl}${endpoint}`, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error(`Erro na API ${endpoint}:`, error);
            throw error;
        }
    }

    // Status do Sistema
    async loadStatus() {
        try {
            const status = await this.apiCall('/status');
            const statusDot = document.getElementById('status-dot');
            const statusText = document.getElementById('status-text');
            
            if (status.running) {
                statusDot?.classList.remove('offline');
                statusDot?.classList.add('online');
                if (statusText) statusText.textContent = 'Sistema Online';
                
                // Atualizar indicador de status
                const statusSubtitle = document.querySelector('.status-subtitle');
                if (statusSubtitle) statusSubtitle.textContent = 'Ativo';
            } else {
                statusDot?.classList.remove('online');
                statusDot?.classList.add('offline');
                if (statusText) statusText.textContent = 'Sistema Offline';
                
                const statusSubtitle = document.querySelector('.status-subtitle');
                if (statusSubtitle) statusSubtitle.textContent = 'Parado';
            }

            // Atualizar botões
            this.updateControlButtons(status.running);
        } catch (error) {
            console.error('Erro ao carregar status:', error);
            const statusText = document.getElementById('status-text');
            if (statusText) statusText.textContent = 'Erro de Conexão';
        }
    }

    updateControlButtons(isRunning) {
        const startBtn = document.getElementById('start-btn');
        const stopBtn = document.getElementById('stop-btn');
        
        if (startBtn) startBtn.disabled = isRunning;
        if (stopBtn) stopBtn.disabled = !isRunning;
    }

    // Estatísticas
    async loadStats() {
        try {
            const stats = await this.apiCall('/stats');
            
            document.getElementById('total-attacks').textContent = stats.totalLogs || '0';
            document.getElementById('ssh-attacks').textContent = stats.sshLogs || '0';
            document.getElementById('telnet-attacks').textContent = stats.telnetLogs || '0';
            
            this.loadUniqueIps();
            
            // Animar números
            this.animateNumbers();
        } catch (error) {
            console.error('Erro ao carregar estatísticas:', error);
            this.setStatsError();
        }
    }

    async loadUniqueIps() {
        try {
            const data = await this.apiCall('/stats/unique-ips');
            const uniqueCount = data.uniqueIpsCount || 0;
            document.getElementById('unique-ips').textContent = uniqueCount.toString();
        } catch (error) {
            console.error('Erro ao carregar IPs únicos:', error);
            document.getElementById('unique-ips').textContent = '0';
        }
    }

    setStatsError() {
        ['total-attacks', 'ssh-attacks', 'telnet-attacks', 'unique-ips'].forEach(id => {
            const element = document.getElementById(id);
            if (element) element.textContent = 'Erro';
        });
    }

    animateNumbers() {
        document.querySelectorAll('.stat-number').forEach(element => {
            element.style.transform = 'scale(1.05)';
            setTimeout(() => {
                element.style.transform = 'scale(1)';
            }, 200);
        });
    }

    // Top IPs
    async loadTopIps() {
        try {
            const data = await this.apiCall('/stats/top-ips?limit=10');
            const container = document.getElementById('top-ips');
            
            if (data.topIps && data.topIps.length > 0) {
                container.innerHTML = data.topIps.map((item, index) => `
                    <div class="top-item">
                        <div class="top-item-info">
                            <div class="top-item-label">
                                <span class="rank">#${index + 1}</span>
                                <code>${item.ip}</code>
                            </div>
                            <div class="top-item-detail">
                                Último ataque: ${this.formatDateTime(item.lastAttack)}
                            </div>
                        </div>
                        <div class="top-item-count">${item.count}</div>
                    </div>
                `).join('');
            } else {
                container.innerHTML = '<div class="loading">Nenhum ataque registrado ainda</div>';
            }
        } catch (error) {
            console.error('Erro ao carregar top IPs:', error);
            document.getElementById('top-ips').innerHTML = '<div class="loading error">Erro ao carregar dados</div>';
        }
    }

    // Top Credenciais
    async loadTopCredentials() {
        try {
            const data = await this.apiCall('/stats/top-credentials?limit=10');
            const container = document.getElementById('top-credentials');
            
            if (data.topCredentials && data.topCredentials.length > 0) {
                container.innerHTML = data.topCredentials.map((item, index) => `
                    <div class="top-item">
                        <div class="top-item-info">
                            <div class="top-item-label">
                                <span class="rank">#${index + 1}</span>
                                <code>${this.truncate(item.username, 12)}:${this.truncate(item.password, 12)}</code>
                            </div>
                            <div class="top-item-detail">Tentativa de credencial</div>
                        </div>
                        <div class="top-item-count">${item.count}</div>
                    </div>
                `).join('');
            } else {
                container.innerHTML = '<div class="loading">Nenhuma credencial registrada ainda</div>';
            }
        } catch (error) {
            console.error('Erro ao carregar top credenciais:', error);
            document.getElementById('top-credentials').innerHTML = '<div class="loading error">Erro ao carregar dados</div>';
        }
    }

    // Logs
    async loadLogs() {
        try {
            const protocolFilter = document.getElementById('protocol-filter')?.value || '';
            const ipFilter = document.getElementById('ip-filter')?.value || '';
            
            let url = `/logs?page=${this.currentPage}&size=${this.pageSize}`;
            
            const data = await this.apiCall(url);
            
            // Armazenar logs atuais para uso no modal
            this.currentLogs = data.logs || [];
            
            const tbody = document.getElementById('logs-tbody');
            
            if (data.logs && data.logs.length > 0) {
                let filteredLogs = data.logs;
                
                // Aplicar filtros no frontend
                if (protocolFilter) {
                    filteredLogs = filteredLogs.filter(log => log.protocol === protocolFilter);
                }
                
                if (ipFilter) {
                    filteredLogs = filteredLogs.filter(log => log.sourceIp.includes(ipFilter));
                }
                
                tbody.innerHTML = filteredLogs.map(log => `
                    <tr class="log-entry" data-protocol="${log.protocol.toLowerCase()}">
                        <td class="timestamp">${this.formatDateTime(log.timestamp)}</td>
                        <td class="ip-address"><code>${log.sourceIp}</code></td>
                        <td class="protocol">
                            <span class="protocol-badge protocol-${log.protocol.toLowerCase()}">
                                ${log.protocol}
                            </span>
                        </td>
                        <td class="username"><code>${this.truncate(log.username || '-', 15)}</code></td>
                        <td class="password"><code>${this.truncate(log.password || '-', 15)}</code></td>
                        <td class="command"><code>${this.truncate(log.commands && log.commands.length > 0 ? log.commands[log.commands.length - 1].command : '-', 20)}</code></td>
                        <td class="actions">
                            <button class="btn-view-commands" 
                                    onclick="dashboard.showCommandsModal('${log.id}')" 
                                    ${!log.commands || log.commands.length === 0 ? 'disabled' : ''}>
                                📋 Ver Comandos (${log.commands ? log.commands.length : 0})
                            </button>
                        </td>
                    </tr>
                `).join('');
            } else {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="7" class="loading">
                            ${protocolFilter || ipFilter ? 'Nenhum registro encontrado com os filtros aplicados' : 'Nenhum registro encontrado'}
                        </td>
                    </tr>
                `;
            }
            
            this.updatePagination(data);
        } catch (error) {
            console.error('Erro ao carregar logs:', error);
            document.getElementById('logs-tbody').innerHTML = `
                <tr><td colspan="7" class="loading error">Erro ao carregar registros</td></tr>
            `;
        }
    }

    updatePagination(data) {
        const pageInfo = document.getElementById('page-info');
        const prevBtn = document.getElementById('prev-page');
        const nextBtn = document.getElementById('next-page');
        
        if (pageInfo) {
            pageInfo.textContent = `Página ${this.currentPage + 1} de ${data.totalPages || 1}`;
        }
        
        if (prevBtn) prevBtn.disabled = this.currentPage === 0;
        if (nextBtn) nextBtn.disabled = this.currentPage >= (data.totalPages - 1);
    }

    previousPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.loadLogs();
        }
    }

    nextPage() {
        this.currentPage++;
        this.loadLogs();
    }

    // Gráficos
    async initCharts() {
        await Promise.all([
            this.createProtocolChart(),
            this.createTimelineChart()
        ]);
    }

    async updateCharts() {
        await Promise.all([
            this.updateProtocolChart(),
            this.updateTimelineChart()
        ]);
    }

    async createProtocolChart() {
        try {
            const stats = await this.apiCall('/stats');
            const ctx = document.getElementById('protocolChart')?.getContext('2d');
            
            if (!ctx) return;
            
            this.charts.protocol = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: ['SSH', 'Telnet'],
                    datasets: [{
                        data: [stats.sshLogs || 0, stats.telnetLogs || 0],
                        backgroundColor: ['#2563eb', '#f59e0b'],
                        borderWidth: 0,
                        hoverBorderWidth: 2,
                        hoverBorderColor: '#ffffff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                padding: 20,
                                usePointStyle: true,
                                font: {
                                    size: 12
                                }
                            }
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const value = context.parsed;
                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                    const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
                                    return `${context.label}: ${value} (${percentage}%)`;
                                }
                            }
                        }
                    }
                }
            });
        } catch (error) {
            console.error('Erro ao criar gráfico de protocolos:', error);
        }
    }

    async createTimelineChart() {
        try {
            const timelineData = await this.apiCall('/stats/timeline');
            const ctx = document.getElementById('timelineChart')?.getContext('2d');
            
            if (!ctx) return;
            
            this.charts.timeline = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: timelineData.hours || [],
                    datasets: [{
                        label: 'Ataques',
                        data: timelineData.counts || [],
                        borderColor: '#2563eb',
                        backgroundColor: 'rgba(37, 99, 235, 0.1)',
                        tension: 0.4,
                        fill: true,
                        pointBackgroundColor: '#2563eb',
                        pointBorderColor: '#ffffff',
                        pointBorderWidth: 2,
                        pointRadius: 4,
                        pointHoverRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                stepSize: 1,
                                font: {
                                    size: 11
                                }
                            },
                            grid: {
                                color: 'rgba(0, 0, 0, 0.05)'
                            }
                        },
                        x: {
                            ticks: {
                                maxTicksLimit: 12,
                                font: {
                                    size: 11
                                }
                            },
                            grid: {
                                color: 'rgba(0, 0, 0, 0.05)'
                            }
                        }
                    },
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            callbacks: {
                                title: function(context) {
                                    return `Hora: ${context[0].label}`;
                                },
                                label: function(context) {
                                    const count = context.parsed.y;
                                    return `${count} ataque${count !== 1 ? 's' : ''}`;
                                }
                            }
                        }
                    }
                }
            });
        } catch (error) {
            console.error('Erro ao criar gráfico de timeline:', error);
        }
    }

    async updateProtocolChart() {
        if (this.charts.protocol) {
            try {
                const stats = await this.apiCall('/stats');
                this.charts.protocol.data.datasets[0].data = [stats.sshLogs || 0, stats.telnetLogs || 0];
                this.charts.protocol.update('none');
            } catch (error) {
                console.error('Erro ao atualizar gráfico de protocolos:', error);
            }
        }
    }

    async updateTimelineChart() {
        if (this.charts.timeline) {
            try {
                const timelineData = await this.apiCall('/stats/timeline');
                this.charts.timeline.data.labels = timelineData.hours || [];
                this.charts.timeline.data.datasets[0].data = timelineData.counts || [];
                this.charts.timeline.update('none');
            } catch (error) {
                console.error('Erro ao atualizar gráfico de timeline:', error);
            }
        }
    }

    // Controles do Sistema
    async startHoneypot() {
        try {
            this.setButtonLoading('start-btn', true);
            await this.apiCall('/start', { method: 'POST' });
            this.showToast('Sistema iniciado com sucesso!', 'success');
            await this.loadStatus();
        } catch (error) {
            this.showToast('Erro ao iniciar sistema', 'error');
        } finally {
            this.setButtonLoading('start-btn', false);
        }
    }

    async stopHoneypot() {
        try {
            this.setButtonLoading('stop-btn', true);
            await this.apiCall('/stop', { method: 'POST' });
            this.showToast('Sistema parado com sucesso!', 'success');
            await this.loadStatus();
        } catch (error) {
            this.showToast('Erro ao parar sistema', 'error');
        } finally {
            this.setButtonLoading('stop-btn', false);
        }
    }

    async restartHoneypot() {
        try {
            this.setButtonLoading('restart-btn', true);
            await this.apiCall('/restart', { method: 'POST' });
            this.showToast('Sistema reiniciado com sucesso!', 'success');
            await this.loadStatus();
        } catch (error) {
            this.showToast('Erro ao reiniciar sistema', 'error');
        } finally {
            this.setButtonLoading('restart-btn', false);
        }
    }

    async clearLogs() {
        if (confirm('Tem certeza que deseja limpar todos os registros? Esta ação não pode ser desfeita.')) {
            try {
                await this.apiCall('/logs', { method: 'DELETE' });
                this.showToast('Registros limpos com sucesso!', 'success');
                await this.refreshAll();
            } catch (error) {
                this.showToast('Erro ao limpar registros', 'error');
            }
        }
    }

    // Utilidades
    setButtonLoading(buttonId, loading) {
        const button = document.getElementById(buttonId);
        if (!button) return;
        
        button.disabled = loading;
        
        if (loading) {
            button.classList.add('loading');
            const icon = button.querySelector('.btn-icon');
            if (icon) icon.textContent = '⏳';
        } else {
            button.classList.remove('loading');
            // Restaurar ícones originais
            const icon = button.querySelector('.btn-icon');
            if (icon && buttonId === 'start-btn') icon.textContent = '▶';
            if (icon && buttonId === 'stop-btn') icon.textContent = '⏹';
            if (icon && buttonId === 'restart-btn') icon.textContent = '🔄';
        }
    }

    setLoadingState(loading) {
        this.isLoading = loading;
        
        if (loading) {
            document.body.classList.add('loading');
        } else {
            document.body.classList.remove('loading');
        }
    }

    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container') || this.createToastContainer();
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
        toast.innerHTML = `
            <div class="toast-icon">${icon}</div>
            <div class="toast-message">${message}</div>
        `;
        
        container.appendChild(toast);
        
        // Auto remove
        setTimeout(() => {
            toast.classList.add('fade-out');
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }

    createToastContainer() {
        const container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
        return container;
    }

    formatDateTime(dateString) {
        try {
            const date = new Date(dateString);
            return date.toLocaleString('pt-BR', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch (error) {
            return dateString;
        }
    }

    truncate(str, maxLength) {
        if (!str || str.length <= maxLength) return str;
        return str.substring(0, maxLength) + '...';
    }

    updateLastUpdate() {
        const element = document.getElementById('last-update');
        if (element) {
            element.textContent = new Date().toLocaleString('pt-BR');
        }
    }

    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // Modal de comandos
    showCommandsModal(logId) {
        // Encontrar o log específico nos dados carregados
        const log = this.currentLogs?.find(l => l.id === logId);
        if (!log) {
            this.showToast('Log não encontrado', 'error');
            return;
        }

        // Preencher informações da sessão
        document.getElementById('modal-ip').textContent = log.sourceIp || '-';
        document.getElementById('modal-protocol').textContent = log.protocol || '-';
        document.getElementById('modal-session').textContent = log.sessionId || '-';
        document.getElementById('modal-command-count').textContent = log.commands ? log.commands.length : 0;

        // Preencher lista de comandos
        const commandsContainer = document.getElementById('commands-container');
        if (!log.commands || log.commands.length === 0) {
            commandsContainer.innerHTML = '<div class="no-commands">Nenhum comando executado nesta sessão</div>';
        } else {
            commandsContainer.innerHTML = log.commands.map((cmd, index) => `
                <div class="command-item">
                    <div class="command-text">${this.escapeHtml(cmd.command || 'comando vazio')}</div>
                    <div class="command-timestamp">${this.formatDateTime(cmd.timestamp)}</div>
                </div>
            `).join('');
        }

        // Mostrar modal
        const modal = document.getElementById('commands-modal');
        modal.classList.add('show');

        // Configurar eventos de fechamento
        this.setupModalEvents();
    }

    setupModalEvents() {
        const modal = document.getElementById('commands-modal');
        const closeBtn = modal.querySelector('.modal-close');

        // Fechar ao clicar no X
        closeBtn.onclick = () => this.closeCommandsModal();

        // Fechar ao clicar fora do modal
        modal.onclick = (e) => {
            if (e.target === modal) {
                this.closeCommandsModal();
            }
        };

        // Fechar com ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && modal.classList.contains('show')) {
                this.closeCommandsModal();
            }
        });
    }

    closeCommandsModal() {
        const modal = document.getElementById('commands-modal');
        modal.classList.remove('show');
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    // ========================================
    // FUNÇÕES DE NOTIFICAÇÕES
    // ========================================
    
    async loadNotifications() {
        try {
            const unreadNotifications = await this.apiCall('/notifications/unread');
            this.renderNotifications(unreadNotifications);
            this.updateUnreadCount(unreadNotifications.length);
        } catch (error) {
            console.error('Erro ao carregar notificações:', error);
        }
    }
    
    renderNotifications(notifications) {
        const container = document.getElementById('notifications-list');
        
        if (!notifications || notifications.length === 0) {
            container.innerHTML = '<div class="no-notifications">Nenhuma notificação não lida</div>';
            return;
        }
        
        container.innerHTML = notifications.map(notification => `
            <div class="notification-item ${notification.read ? '' : 'unread'} ${notification.isHighPriority ? 'high-priority' : ''} ${notification.isCritical ? 'critical' : ''} notification-type-${notification.type.toLowerCase()}">
                <div class="notification-icon">
                    ${this.getNotificationIcon(notification.type)}
                </div>
                <div class="notification-content">
                    <div class="notification-title">${this.escapeHtml(notification.title)}</div>
                    <div class="notification-message">${this.escapeHtml(notification.message)}</div>
                    <div class="notification-meta">
                        <span class="notification-timestamp">${this.formatDateTime(notification.timestamp)}</span>
                        ${notification.sourceIp ? `<span>IP: ${notification.sourceIp}</span>` : ''}
                        ${notification.protocol ? `<span>Protocolo: ${notification.protocol}</span>` : ''}
                    </div>
                </div>
                <div class="notification-actions">
                    ${!notification.read ? `<button class="btn-mark-read" onclick="dashboard.markNotificationAsRead('${notification.id}')">Marcar como Lida</button>` : ''}
                </div>
            </div>
        `).join('');
    }
    
    getNotificationIcon(type) {
        const icons = {
            'INFO': 'ℹ️',
            'WARNING': '⚠️',
            'ERROR': '❌',
            'SUCCESS': '✅',
            'ALERT': '🚨'
        };
        return icons[type] || '📢';
    }
    
    updateUnreadCount(count) {
        const badge = document.getElementById('unread-count');
        if (badge) {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline-block' : 'none';
        }
    }
    
    async markNotificationAsRead(id) {
        try {
            await this.apiCall(`/notifications/${id}/read`, { method: 'PUT' });
            this.showToast('Notificação marcada como lida', 'success');
            this.loadNotifications(); // Recarregar notificações
        } catch (error) {
            console.error('Erro ao marcar notificação como lida:', error);
            this.showToast('Erro ao marcar notificação como lida', 'error');
        }
    }
    
    async markAllNotificationsAsRead() {
        try {
            await this.apiCall('/notifications/read-all', { method: 'PUT' });
            this.showToast('Todas as notificações foram marcadas como lidas', 'success');
            this.loadNotifications(); // Recarregar notificações
        } catch (error) {
            console.error('Erro ao marcar todas as notificações como lidas:', error);
            this.showToast('Erro ao marcar todas como lidas', 'error');
        }
    }
    
    async showAllNotificationsModal() {
        try {
            const allNotifications = await this.apiCall('/notifications?page=0&size=100');
            this.renderAllNotificationsModal(allNotifications.notifications || []);
            
            const modal = document.getElementById('all-notifications-modal');
            modal.classList.add('show');
            this.setupAllNotificationsModalEvents();
        } catch (error) {
            console.error('Erro ao carregar todas as notificações:', error);
            this.showToast('Erro ao carregar notificações', 'error');
        }
    }
    
    renderAllNotificationsModal(notifications) {
        const container = document.getElementById('all-notifications-container');
        
        if (!notifications || notifications.length === 0) {
            container.innerHTML = '<div class="no-notifications">Nenhuma notificação encontrada</div>';
            return;
        }
        
        container.innerHTML = notifications.map(notification => `
            <div class="notification-item ${notification.read ? '' : 'unread'} ${notification.isHighPriority ? 'high-priority' : ''} ${notification.isCritical ? 'critical' : ''} notification-type-${notification.type.toLowerCase()}">
                <div class="notification-icon">
                    ${this.getNotificationIcon(notification.type)}
                </div>
                <div class="notification-content">
                    <div class="notification-title">${this.escapeHtml(notification.title)}</div>
                    <div class="notification-message">${this.escapeHtml(notification.message)}</div>
                    <div class="notification-meta">
                        <span class="notification-timestamp">${this.formatDateTime(notification.timestamp)}</span>
                        ${notification.sourceIp ? `<span>IP: ${notification.sourceIp}</span>` : ''}
                        ${notification.protocol ? `<span>Protocolo: ${notification.protocol}</span>` : ''}
                        <span>Categoria: ${notification.category}</span>
                    </div>
                </div>
                <div class="notification-actions">
                    ${!notification.read ? `<button class="btn-mark-read" onclick="dashboard.markNotificationAsRead('${notification.id}')">Marcar como Lida</button>` : ''}
                </div>
            </div>
        `).join('');
    }
    
    setupAllNotificationsModalEvents() {
        const modal = document.getElementById('all-notifications-modal');
        const closeBtn = modal.querySelector('.modal-close');
        
        // Fechar ao clicar no X
        closeBtn.onclick = () => this.closeAllNotificationsModal();
        
        // Fechar ao clicar fora do modal
        modal.onclick = (e) => {
            if (e.target === modal) {
                this.closeAllNotificationsModal();
            }
        };
        
        // Fechar com ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && modal.classList.contains('show')) {
                this.closeAllNotificationsModal();
            }
        });
        
        // Filtros
        document.getElementById('clear-notification-filters')?.addEventListener('click', () => this.clearNotificationFilters());
        
        // Aplicar filtros automaticamente com debounce
        let filterTimeout;
        const applyFiltersWithDelay = () => {
            clearTimeout(filterTimeout);
            filterTimeout = setTimeout(() => this.filterNotifications(), 300);
        };
        
        document.getElementById('notification-type-filter')?.addEventListener('change', applyFiltersWithDelay);
        document.getElementById('notification-category-filter')?.addEventListener('change', applyFiltersWithDelay);
    }
    
    closeAllNotificationsModal() {
        const modal = document.getElementById('all-notifications-modal');
        modal.classList.remove('show');
    }
    
    async filterNotifications() {
        try {
            const typeFilter = document.getElementById('notification-type-filter').value;
            const categoryFilter = document.getElementById('notification-category-filter').value;
            
            // Atualizar indicador de filtros ativos
            this.updateActiveFiltersIndicator(typeFilter, categoryFilter);
            
            // Se não há filtros ativos, recarregar todas as notificações
            if (!typeFilter && !categoryFilter) {
                this.showAllNotificationsModal();
                return;
            }
            
            // Construir URL com filtros
            let url = '/notifications?page=0&size=100';
            if (typeFilter) {
                url += `&type=${encodeURIComponent(typeFilter)}`;
            }
            if (categoryFilter) {
                url += `&category=${encodeURIComponent(categoryFilter)}`;
            }
            
            const response = await this.apiCall(url);
            const filteredNotifications = response.notifications || [];
            
            // Renderizar notificações filtradas
            this.renderAllNotificationsModal(filteredNotifications);
            
            // Mostrar feedback visual
            const filterInfo = [];
            if (typeFilter) filterInfo.push(`Tipo: ${typeFilter}`);
            if (categoryFilter) filterInfo.push(`Categoria: ${categoryFilter}`);
            
            if (filterInfo.length > 0) {
                this.showToast(`Filtros aplicados: ${filterInfo.join(', ')}`, 'info');
            }
            
        } catch (error) {
            console.error('Erro ao filtrar notificações:', error);
            this.showToast('Erro ao aplicar filtros', 'error');
        }
    }
    
    updateActiveFiltersIndicator(typeFilter, categoryFilter) {
        const indicator = document.getElementById('active-filters-indicator');
        if (indicator) {
            if (typeFilter || categoryFilter) {
                indicator.style.display = 'inline-flex';
                const filterText = [];
                if (typeFilter) filterText.push(typeFilter);
                if (categoryFilter) filterText.push(categoryFilter);
                indicator.textContent = `Filtros: ${filterText.join(', ')}`;
            } else {
                indicator.style.display = 'none';
            }
        }
    }
    
    clearNotificationFilters() {
        document.getElementById('notification-type-filter').value = '';
        document.getElementById('notification-category-filter').value = '';
        
        // Atualizar indicador de filtros ativos
        this.updateActiveFiltersIndicator('', '');
        
        // Recarregar notificações sem filtros
        this.showAllNotificationsModal();
        
        // Mostrar feedback
        this.showToast('Filtros removidos', 'info');
    }
}

// Inicializar dashboard quando a página carregar
let dashboard;
document.addEventListener('DOMContentLoaded', () => {
    dashboard = new SecurityDashboard();
});