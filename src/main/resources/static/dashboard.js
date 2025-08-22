// JavaScript para HoneyPot Dashboard
class HoneyPotDashboard {
    constructor() {
        this.apiUrl = '/api/honeypot';
        this.currentPage = 0;
        this.pageSize = 20;
        this.refreshInterval = 30000; // 30 segundos
        this.charts = {};
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadInitialData();
        this.startAutoRefresh();
    }

    setupEventListeners() {
        // Controles da honeypot
        document.getElementById('start-btn').addEventListener('click', () => this.startHoneypot());
        document.getElementById('stop-btn').addEventListener('click', () => this.stopHoneypot());
        document.getElementById('restart-btn').addEventListener('click', () => this.restartHoneypot());
        document.getElementById('refresh-btn').addEventListener('click', () => this.refreshAll());

        // Filtros de logs
        document.getElementById('protocol-filter').addEventListener('change', () => this.loadLogs());
        document.getElementById('ip-filter').addEventListener('input', () => this.debounce(() => this.loadLogs(), 500)());
        document.getElementById('clear-logs-btn').addEventListener('click', () => this.clearLogs());

        // Paginação
        document.getElementById('prev-page').addEventListener('click', () => this.previousPage());
        document.getElementById('next-page').addEventListener('click', () => this.nextPage());
    }

    async loadInitialData() {
        await Promise.all([
            this.loadStatus(),
            this.loadStats(),
            this.loadTopIps(),
            this.loadTopCredentials(),
            this.loadLogs(),
            this.initCharts()
        ]);
    }

    startAutoRefresh() {
        setInterval(() => {
            this.refreshAll();
        }, this.refreshInterval);
    }

    async refreshAll() {
        try {
            await Promise.all([
                this.loadStatus(),
                this.loadStats(),
                this.loadTopIps(),
                this.loadTopCredentials(),
                this.loadLogs(),
                this.updateCharts()
            ]);
            this.updateLastUpdate();
            this.showToast('Dashboard atualizado com sucesso!', 'success');
        } catch (error) {
            console.error('Erro ao atualizar dashboard:', error);
            this.showToast('Erro ao atualizar dados', 'error');
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

    async loadStatus() {
        try {
            const status = await this.apiCall('/status');
            const statusDot = document.getElementById('status-dot');
            const statusText = document.getElementById('status-text');
            
            if (status.running) {
                statusDot.className = 'status-dot online';
                statusText.textContent = 'Online - Capturando ataques';
            } else {
                statusDot.className = 'status-dot offline';
                statusText.textContent = 'Offline';
            }

            // Atualizar botões
            document.getElementById('start-btn').disabled = status.running;
            document.getElementById('stop-btn').disabled = !status.running;
        } catch (error) {
            console.error('Erro ao carregar status:', error);
            document.getElementById('status-text').textContent = 'Erro de conexão';
        }
    }

    async loadStats() {
        try {
            const stats = await this.apiCall('/stats');
            
            document.getElementById('total-attacks').textContent = stats.totalLogs || 0;
            document.getElementById('ssh-attacks').textContent = stats.sshLogs || 0;
            document.getElementById('telnet-attacks').textContent = stats.telnetLogs || 0;
            
            // Calcular IPs únicos (aproximação baseada nos dados disponíveis)
            const uniqueIps = Math.max(1, Math.floor((stats.totalLogs || 0) * 0.3));
            document.getElementById('unique-ips').textContent = uniqueIps;
        } catch (error) {
            console.error('Erro ao carregar estatísticas:', error);
        }
    }

    async loadTopIps() {
        try {
            const data = await this.apiCall('/stats/top-ips?limit=10');
            const container = document.getElementById('top-ips');
            
            if (data.topIps && data.topIps.length > 0) {
                container.innerHTML = data.topIps.map((item, index) => `
                    <div class="top-item">
                        <div class="top-item-info">
                            <div class="top-item-label">${item.ip}</div>
                            <div class="top-item-detail">Último ataque: ${this.formatDateTime(item.lastAttack)}</div>
                        </div>
                        <div class="top-item-count">${item.count}</div>
                    </div>
                `).join('');
            } else {
                container.innerHTML = '<div class="loading">Nenhum ataque registrado</div>';
            }
        } catch (error) {
            console.error('Erro ao carregar top IPs:', error);
            document.getElementById('top-ips').innerHTML = '<div class="loading">Erro ao carregar dados</div>';
        }
    }

    async loadTopCredentials() {
        try {
            const data = await this.apiCall('/stats/top-credentials?limit=10');
            const container = document.getElementById('top-credentials');
            
            if (data.topCredentials && data.topCredentials.length > 0) {
                container.innerHTML = data.topCredentials.map((item, index) => `
                    <div class="top-item">
                        <div class="top-item-info">
                            <div class="top-item-label">${item.username} / ${item.password}</div>
                            <div class="top-item-detail">Credencial tentada</div>
                        </div>
                        <div class="top-item-count">${item.count}</div>
                    </div>
                `).join('');
            } else {
                container.innerHTML = '<div class="loading">Nenhuma credencial registrada</div>';
            }
        } catch (error) {
            console.error('Erro ao carregar top credenciais:', error);
            document.getElementById('top-credentials').innerHTML = '<div class="loading">Erro ao carregar dados</div>';
        }
    }

    async loadLogs() {
        try {
            const protocolFilter = document.getElementById('protocol-filter').value;
            const ipFilter = document.getElementById('ip-filter').value;
            
            let url = `/logs?page=${this.currentPage}&size=${this.pageSize}`;
            
            const data = await this.apiCall(url);
            const tbody = document.getElementById('logs-tbody');
            
            if (data.logs && data.logs.length > 0) {
                // Filtrar logs no frontend se necessário
                let filteredLogs = data.logs;
                
                if (protocolFilter) {
                    filteredLogs = filteredLogs.filter(log => log.protocol === protocolFilter);
                }
                
                if (ipFilter) {
                    filteredLogs = filteredLogs.filter(log => log.sourceIp.includes(ipFilter));
                }
                
                tbody.innerHTML = filteredLogs.map(log => `
                    <tr>
                        <td>${this.formatDateTime(log.timestamp)}</td>
                        <td><code>${log.sourceIp}</code></td>
                        <td><span class="protocol-badge protocol-${log.protocol.toLowerCase()}">${log.protocol}</span></td>
                        <td><code>${this.truncate(log.username || '-', 20)}</code></td>
                        <td><code>${this.truncate(log.password || '-', 20)}</code></td>
                        <td><code>${this.truncate(log.command || '-', 30)}</code></td>
                    </tr>
                `).join('');
            } else {
                tbody.innerHTML = '<tr><td colspan="6" class="loading">Nenhum log encontrado</td></tr>';
            }
            
            // Atualizar paginação
            this.updatePagination(data);
        } catch (error) {
            console.error('Erro ao carregar logs:', error);
            document.getElementById('logs-tbody').innerHTML = '<tr><td colspan="6" class="loading">Erro ao carregar logs</td></tr>';
        }
    }

    updatePagination(data) {
        const pageInfo = document.getElementById('page-info');
        const prevBtn = document.getElementById('prev-page');
        const nextBtn = document.getElementById('next-page');
        
        pageInfo.textContent = `Página ${this.currentPage + 1} de ${data.totalPages || 1}`;
        prevBtn.disabled = this.currentPage === 0;
        nextBtn.disabled = this.currentPage >= (data.totalPages - 1);
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

    async initCharts() {
        await this.createProtocolChart();
        await this.createTimelineChart();
    }

    async updateCharts() {
        await this.updateProtocolChart();
        await this.updateTimelineChart();
    }

    async createProtocolChart() {
        try {
            const stats = await this.apiCall('/stats');
            const ctx = document.getElementById('protocolChart').getContext('2d');
            
            this.charts.protocol = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: ['SSH', 'Telnet'],
                    datasets: [{
                        data: [stats.sshLogs || 0, stats.telnetLogs || 0],
                        backgroundColor: ['#ff6b35', '#f7931e'],
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'bottom'
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
            const ctx = document.getElementById('timelineChart').getContext('2d');
            
            this.charts.timeline = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: timelineData.hours || [],
                    datasets: [{
                        label: 'Ataques por Hora',
                        data: timelineData.counts || [],
                        borderColor: '#ff6b35',
                        backgroundColor: 'rgba(255, 107, 53, 0.1)',
                        tension: 0.4,
                        fill: true,
                        pointBackgroundColor: '#ff6b35',
                        pointBorderColor: '#ffffff',
                        pointBorderWidth: 2,
                        pointRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                stepSize: 1
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
            // Fallback para dados vazios se houver erro
            const ctx = document.getElementById('timelineChart').getContext('2d');
            const hours = Array.from({length: 24}, (_, i) => `${i.toString().padStart(2, '0')}:00`);
            const data = Array.from({length: 24}, () => 0);
            
            this.charts.timeline = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: hours,
                    datasets: [{
                        label: 'Ataques por Hora',
                        data: data,
                        borderColor: '#ff6b35',
                        backgroundColor: 'rgba(255, 107, 53, 0.1)',
                        tension: 0.4,
                        fill: true
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    },
                    plugins: {
                        legend: {
                            display: false
                        }
                    }
                }
            });
        }
    }

    async updateProtocolChart() {
        if (this.charts.protocol) {
            try {
                const stats = await this.apiCall('/stats');
                this.charts.protocol.data.datasets[0].data = [stats.sshLogs || 0, stats.telnetLogs || 0];
                this.charts.protocol.update();
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
                this.charts.timeline.update();
            } catch (error) {
                console.error('Erro ao atualizar gráfico de timeline:', error);
            }
        }
    }

    async startHoneypot() {
        try {
            this.setButtonLoading('start-btn', true);
            await this.apiCall('/start', { method: 'POST' });
            this.showToast('Honeypot iniciada com sucesso!', 'success');
            await this.loadStatus();
        } catch (error) {
            this.showToast('Erro ao iniciar honeypot', 'error');
        } finally {
            this.setButtonLoading('start-btn', false);
        }
    }

    async stopHoneypot() {
        try {
            this.setButtonLoading('stop-btn', true);
            await this.apiCall('/stop', { method: 'POST' });
            this.showToast('Honeypot parada com sucesso!', 'success');
            await this.loadStatus();
        } catch (error) {
            this.showToast('Erro ao parar honeypot', 'error');
        } finally {
            this.setButtonLoading('stop-btn', false);
        }
    }

    async restartHoneypot() {
        try {
            this.setButtonLoading('restart-btn', true);
            await this.apiCall('/restart', { method: 'POST' });
            this.showToast('Honeypot reiniciada com sucesso!', 'success');
            await this.loadStatus();
        } catch (error) {
            this.showToast('Erro ao reiniciar honeypot', 'error');
        } finally {
            this.setButtonLoading('restart-btn', false);
        }
    }

    async clearLogs() {
        if (confirm('Tem certeza que deseja limpar todos os logs? Esta ação não pode ser desfeita.')) {
            try {
                await this.apiCall('/logs', { method: 'DELETE' });
                this.showToast('Logs limpos com sucesso!', 'success');
                await this.refreshAll();
            } catch (error) {
                this.showToast('Erro ao limpar logs', 'error');
            }
        }
    }

    setButtonLoading(buttonId, loading) {
        const button = document.getElementById(buttonId);
        button.disabled = loading;
        if (loading) {
            button.style.opacity = '0.6';
        } else {
            button.style.opacity = '1';
        }
    }

    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        
        container.appendChild(toast);
        
        setTimeout(() => {
            toast.remove();
        }, 5000);
    }

    formatDateTime(dateString) {
        try {
            const date = new Date(dateString);
            return date.toLocaleString('pt-BR');
        } catch (error) {
            return dateString;
        }
    }

    truncate(str, maxLength) {
        if (str.length > maxLength) {
            return str.substring(0, maxLength) + '...';
        }
        return str;
    }

    updateLastUpdate() {
        document.getElementById('last-update').textContent = new Date().toLocaleString('pt-BR');
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
}

// Inicializar dashboard quando a página carregar
document.addEventListener('DOMContentLoaded', () => {
    new HoneyPotDashboard();
});
