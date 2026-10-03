import { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Card,
  CardContent,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  Computer,
  People,
  CloudQueue,
  Memory,
} from '@mui/icons-material';
import AdminLayout from '../../components/AdminLayout';
import { organizationService } from '../../services/organization';

function Dashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const response = await organizationService.getStats();
      if (response.success) {
        setStats(response.data);
      } else {
        setError('Failed to load statistics');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load statistics');
    } finally {
      setLoading(false);
    }
  };

  const StatCard = ({ title, value, subtitle, icon, color }) => (
    <Card elevation={2}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Box
            sx={{
              bgcolor: `${color}.light`,
              p: 1,
              borderRadius: 2,
              mr: 2,
              display: 'flex',
            }}
          >
            {icon}
          </Box>
          <Typography variant="h6" color="text.secondary">
            {title}
          </Typography>
        </Box>
        <Typography variant="h3" fontWeight="bold">
          {value}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          {subtitle}
        </Typography>
      </CardContent>
    </Card>
  );

  if (loading) {
    return (
      <AdminLayout>
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      </AdminLayout>
    );
  }

  if (error) {
    return (
      <AdminLayout>
        <Alert severity="error">{error}</Alert>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      <Typography variant="h4" gutterBottom fontWeight="bold">
        Dashboard
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        {stats?.organizationName || 'Organization'} - Overview
      </Typography>

      <Grid container spacing={3}>
        {/* Device Stats */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Devices Online"
            value={`${stats?.devices?.online || 0}/${stats?.devices?.total || 0}`}
            subtitle={`${stats?.devices?.offline || 0} offline, ${stats?.devices?.busy || 0} busy`}
            icon={<Computer color="primary" />}
            color="primary"
          />
        </Grid>

        {/* Student Stats */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active Students"
            value={stats?.students?.approved || 0}
            subtitle={`${stats?.students?.pending || 0} pending approval`}
            icon={<People color="success" />}
            color="success"
          />
        </Grid>

        {/* Container Stats */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Containers Running"
            value={stats?.containers?.running || 0}
            subtitle={`${stats?.containers?.total || 0} total containers`}
            icon={<CloudQueue color="info" />}
            color="info"
          />
        </Grid>

        {/* CPU Usage */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="CPU Utilization"
            value={`${stats?.resources?.cpuUtilizationPercent?.toFixed(1) || 0}%`}
            subtitle={`${stats?.resources?.cpuAvailable || 0} cores available`}
            icon={<Memory color="warning" />}
            color="warning"
          />
        </Grid>
      </Grid>

      {/* Resource Details */}
      <Grid container spacing={3} sx={{ mt: 2 }}>
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Resource Summary
            </Typography>
            <Box sx={{ mt: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">Total CPU Cores</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {stats?.resources?.cpuTotal || 0}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">CPU Used</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {stats?.resources?.cpuUsed || 0}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">CPU Available</Typography>
                <Typography variant="body2" fontWeight="bold" color="success.main">
                  {stats?.resources?.cpuAvailable || 0}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1, mt: 2 }}>
                <Typography variant="body2">Total RAM</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {stats?.resources?.ramTotal || 0} GB
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">RAM Used</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {stats?.resources?.ramUsed || 0} GB
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">RAM Available</Typography>
                <Typography variant="body2" fontWeight="bold" color="success.main">
                  {stats?.resources?.ramAvailable || 0} GB
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
                <Typography variant="body2">RAM Utilization</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {stats?.resources?.ramUtilizationPercent?.toFixed(1) || 0}%
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Quick Stats
            </Typography>
            <Box sx={{ mt: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">Total Devices</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {stats?.devices?.total || 0}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">Devices in Maintenance</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {stats?.devices?.maintenance || 0}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1, mt: 2 }}>
                <Typography variant="body2">Total Students</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {stats?.students?.total || 0}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">Active Students</Typography>
                <Typography variant="body2" fontWeight="bold" color="success.main">
                  {stats?.students?.active || 0}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1, mt: 2 }}>
                <Typography variant="body2">Containers Stopped</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {stats?.containers?.stopped || 0}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">Containers Pending</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {stats?.containers?.pending || 0}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">Containers Failed</Typography>
                <Typography variant="body2" fontWeight="bold" color="error.main">
                  {stats?.containers?.failed || 0}
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </AdminLayout>
  );
}

export default Dashboard;
