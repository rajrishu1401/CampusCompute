import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  CircularProgress,
  Alert,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material';
import { Add, ContentCopy } from '@mui/icons-material';
import AdminLayout from '../../components/AdminLayout';
import { organizationService } from '../../services/organization';

function Devices() {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tokenDialog, setTokenDialog] = useState(false);
  const [enrollmentToken, setEnrollmentToken] = useState('');

  useEffect(() => {
    fetchDevices();
  }, []);

  const fetchDevices = async () => {
    try {
      const response = await organizationService.getDevices();
      if (response.success) {
        setDevices(response.data);
      } else {
        setError('Failed to load devices');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load devices');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateToken = async () => {
    try {
      const response = await organizationService.generateEnrollmentToken();
      if (response.success) {
        // Response.data is EnrollmentTokenResponse object with token, installScript, etc.
        setEnrollmentToken(response.data);
        setTokenDialog(true);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate token');
    }
  };

  const handleCopyToken = () => {
    const tokenString = typeof enrollmentToken === 'string' 
      ? enrollmentToken 
      : enrollmentToken.token;
    navigator.clipboard.writeText(tokenString);
  };

  const handleCopyInstallScript = () => {
    if (enrollmentToken.installScript) {
      navigator.clipboard.writeText(enrollmentToken.installScript);
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      ONLINE: 'success',
      OFFLINE: 'error',
      BUSY: 'warning',
      MAINTENANCE: 'default',
    };
    return colors[status] || 'default';
  };

  if (loading) {
    return (
      <AdminLayout>
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" gutterBottom fontWeight="bold">
            Devices
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage your organization's devices
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleGenerateToken}
        >
          Add Device
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Hostname</strong></TableCell>
              <TableCell><strong>Status</strong></TableCell>
              <TableCell><strong>CPU</strong></TableCell>
              <TableCell><strong>RAM</strong></TableCell>
              <TableCell><strong>Disk</strong></TableCell>
              <TableCell><strong>Containers</strong></TableCell>
              <TableCell><strong>Last Heartbeat</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {devices.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography variant="body2" color="text.secondary">
                    No devices found. Click "Add Device" to get started.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              devices.map((device) => (
                <TableRow key={device.id}>
                  <TableCell>{device.hostname}</TableCell>
                  <TableCell>
                    <Chip
                      label={device.status}
                      color={getStatusColor(device.status)}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {device.availableCpuCores}/{device.totalCpuCores} cores
                  </TableCell>
                  <TableCell>
                    {device.availableRamGb}/{device.totalRamGb} GB
                  </TableCell>
                  <TableCell>
                    {device.diskUsagePercent?.toFixed(1)}%
                  </TableCell>
                  <TableCell>{device.activeContainers}</TableCell>
                  <TableCell>
                    {device.lastHeartbeat
                      ? new Date(device.lastHeartbeat).toLocaleString()
                      : 'Never'}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Enrollment Token Dialog */}
      <Dialog open={tokenDialog} onClose={() => setTokenDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>Device Enrollment Token</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Use this token to enroll a new device. Copy the token and configure your agent.
          </Typography>
          
          <Typography variant="subtitle2" sx={{ mb: 1, mt: 2 }}>
            Enrollment Token:
          </Typography>
          <TextField
            fullWidth
            multiline
            rows={2}
            value={typeof enrollmentToken === 'string' ? enrollmentToken : enrollmentToken?.token || ''}
            InputProps={{
              readOnly: true,
            }}
            sx={{ mb: 2 }}
          />
          <Button 
            variant="outlined" 
            startIcon={<ContentCopy />}
            onClick={handleCopyToken}
            sx={{ mb: 3 }}
          >
            Copy Token
          </Button>

          {enrollmentToken?.installScript && (
            <>
              <Typography variant="subtitle2" sx={{ mb: 1, mt: 2 }}>
                Quick Setup (Copy and run on device):
              </Typography>
              <TextField
                fullWidth
                multiline
                rows={4}
                value={enrollmentToken.installScript}
                InputProps={{
                  readOnly: true,
                  sx: { fontFamily: 'monospace', fontSize: '0.875rem' }
                }}
                sx={{ mb: 2 }}
              />
              <Button 
                variant="outlined" 
                startIcon={<ContentCopy />}
                onClick={handleCopyInstallScript}
              >
                Copy Install Script
              </Button>
            </>
          )}

          <Alert severity="warning" sx={{ mt: 3 }}>
            This token will expire on {enrollmentToken?.expiresAt 
              ? new Date(enrollmentToken.expiresAt).toLocaleString()
              : '24 hours'
            } and can only be used once.
          </Alert>

          <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
            <Typography variant="subtitle2" gutterBottom>
              Manual Configuration:
            </Typography>
            <Typography variant="body2" component="pre" sx={{ fontFamily: 'monospace', fontSize: '0.75rem', whiteSpace: 'pre-wrap' }}>
{`1. Edit agent/config.yaml:
   broker:
     url: "${enrollmentToken?.backendUrl || 'ws://localhost:8081'}/ws/agent"
     enrollment_token: "${typeof enrollmentToken === 'string' ? enrollmentToken : enrollmentToken?.token || ''}"
     organization_id: ${enrollmentToken?.organizationId || ''}

2. Start agent:
   python src/main.py`}
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTokenDialog(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </AdminLayout>
  );
}

export default Devices;
