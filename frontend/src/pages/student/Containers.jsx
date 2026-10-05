import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
  Grid,
  IconButton,
} from '@mui/material';
import { Add, PlayArrow, Stop, Delete, Terminal } from '@mui/icons-material';
import StudentLayout from '../../components/StudentLayout';
import { containerService } from '../../services/container';

function Containers() {
  const navigate = useNavigate();
  const [containers, setContainers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [createDialog, setCreateDialog] = useState(false);
  const [creating, setCreating] = useState(false);
  const [actionLoading, setActionLoading] = useState(null); // Track which container is being acted upon
  const [newContainer, setNewContainer] = useState({
    image: 'ubuntu:latest',
    cpuCores: 1,
    ramBytes: 1 * 1024 * 1024 * 1024, // 1GB in bytes
    diskBytes: 10 * 1024 * 1024 * 1024, // 10GB in bytes
    lifetimeMs: 4 * 60 * 60 * 1000, // 4 hours in milliseconds
  });

  useEffect(() => {
    fetchContainers();
  }, []);

  const fetchContainers = async (showLoading = true) => {
    if (showLoading) {
      setLoading(true);
    }
    console.log('fetchContainers called, showLoading:', showLoading);
    try {
      const response = await containerService.getContainers();
      console.log('fetchContainers response:', response);
      if (response.success) {
        console.log('Setting containers:', response.data);
        setContainers(response.data);
        setError(''); // Clear any previous errors
      } else {
        setError('Failed to load containers');
      }
    } catch (err) {
      console.error('fetchContainers error:', err);
      setError(err.response?.data?.message || 'Failed to load containers');
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  };

  const handleCreateContainer = async () => {
    setCreating(true);
    setError('');
    setSuccess('');
    try {
      const response = await containerService.createContainer(newContainer);
      if (response.success) {
        setCreateDialog(false);
        setSuccess('Container created successfully');
        setNewContainer({
          image: 'ubuntu:latest',
          cpuCores: 1,
          ramBytes: 1 * 1024 * 1024 * 1024,
          diskBytes: 10 * 1024 * 1024 * 1024,
          lifetimeMs: 4 * 60 * 60 * 1000,
        });
        fetchContainers(false);
      } else {
        setError(response.message || 'Failed to create container');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create container');
    } finally {
      setCreating(false);
    }
  };

  const handleStopContainer = async (id) => {
    setError('');
    setSuccess('');
    setActionLoading(id);
    
    try {
      console.log('Stopping container:', id);
      const response = await containerService.stopContainer(id);
      console.log('Stop response:', response);
      
      if (response.success) {
        setSuccess('Container is stopping...');
        console.log('Fetching updated containers...');
        // Fetch immediately to show STOPPING status
        await fetchContainers(false);
        console.log('Containers updated');
        
        // Poll for final status after agent processes
        setTimeout(async () => {
          console.log('Checking final stop status...');
          await fetchContainers(false);
          setSuccess('Container stopped successfully');
        }, 10000);
      } else {
        setError(response.message || 'Failed to stop container');
      }
    } catch (err) {
      console.error('Stop error:', err);
      setError(err.response?.data?.message || err.message || 'Failed to stop container');
    } finally {
      setActionLoading(null);
    }
  };

  const handleRestartContainer = async (id) => {
    setError('');
    setSuccess('');
    setActionLoading(id);
    
    try {
      console.log('Restarting container:', id);
      const response = await containerService.restartContainer(id);
      console.log('Restart response:', response);
      
      if (response.success) {
        setSuccess('Container is starting...');
        console.log('Fetching updated containers...');
        // Fetch immediately to show RESTARTING status
        await fetchContainers(false);
        console.log('Containers updated');
        
        // Poll for final status after agent processes
        setTimeout(async () => {
          console.log('Checking final restart status...');
          await fetchContainers(false);
          setSuccess('Container is now running');
        }, 10000);
      } else {
        setError(response.message || 'Failed to restart container');
      }
    } catch (err) {
      console.error('Restart error:', err);
      setError(err.response?.data?.message || err.message || 'Failed to restart container');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDeleteContainer = async (id) => {
    console.log('Delete clicked for container ID:', id);
    if (window.confirm('Are you sure you want to delete this container?')) {
      console.log('Delete confirmed, sending request...');
      setError('');
      setSuccess('');
      setActionLoading(id);
      try {
        const response = await containerService.deleteContainer(id);
        console.log('Delete response:', response);
        if (response.success) {
          setSuccess('Container deleted successfully');
        }
        fetchContainers(false);
      } catch (err) {
        console.error('Delete error:', err);
        setError(err.response?.data?.message || 'Failed to delete container');
      } finally {
        setActionLoading(null);
      }
    } else {
      console.log('Delete cancelled by user');
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      RUNNING: 'success',
      STOPPED: 'default',
      STOPPING: 'warning',
      RESTARTING: 'info',
      PENDING: 'warning',
      CREATING: 'info',
      FAILED: 'error',
    };
    return colors[status] || 'default';
  };

  const getStatusIcon = (status) => {
    if (status === 'STOPPING' || status === 'RESTARTING' || status === 'CREATING') {
      return <CircularProgress size={16} sx={{ ml: 1 }} />;
    }
    return null;
  };

  if (loading) {
    return (
      <StudentLayout>
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      </StudentLayout>
    );
  }

  return (
    <StudentLayout>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" gutterBottom fontWeight="bold">
            My Containers
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Create and manage your containers
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => setCreateDialog(true)}
        >
          Create Container
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Image</strong></TableCell>
              <TableCell><strong>Status</strong></TableCell>
              <TableCell><strong>CPU</strong></TableCell>
              <TableCell><strong>RAM</strong></TableCell>
              <TableCell><strong>Device</strong></TableCell>
              <TableCell><strong>Actions</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {containers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography variant="body2" color="text.secondary">
                    No containers found. Click "Create Container" to get started.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              containers.map((container) => (
                <TableRow key={container.id}>
                  <TableCell>{container.image}</TableCell>
                  <TableCell>
                    <Chip
                      label={container.status}
                      color={getStatusColor(container.status)}
                      size="small"
                      icon={getStatusIcon(container.status)}
                    />
                  </TableCell>
                  <TableCell>{container.allocatedCpuCores} cores</TableCell>
                  <TableCell>{(container.allocatedRamBytes / (1024 * 1024 * 1024)).toFixed(1)} GB</TableCell>
                  <TableCell>{container.device?.hostname || 'N/A'}</TableCell>
                  <TableCell>
                    {container.status === 'RUNNING' && (
                      <>
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => navigate(`/student/terminal/${container.id}`)}
                          title="Open Terminal"
                          disabled={actionLoading !== null}
                        >
                          <Terminal />
                        </IconButton>
                        <IconButton
                          size="small"
                          color="warning"
                          onClick={() => handleStopContainer(container.id)}
                          title="Stop"
                          disabled={actionLoading !== null}
                        >
                          {actionLoading === container.id ? (
                            <CircularProgress size={20} />
                          ) : (
                            <Stop />
                          )}
                        </IconButton>
                      </>
                    )}
                    {(container.status === 'STOPPING' || container.status === 'RESTARTING') && (
                      <IconButton
                        size="small"
                        disabled
                        title={container.status === 'STOPPING' ? 'Stopping...' : 'Restarting...'}
                      >
                        <CircularProgress size={20} />
                      </IconButton>
                    )}
                    {container.status === 'STOPPED' && (
                      <IconButton
                        size="small"
                        color="success"
                        onClick={() => handleRestartContainer(container.id)}
                        title="Restart"
                        disabled={actionLoading !== null}
                      >
                        {actionLoading === container.id ? (
                          <CircularProgress size={20} />
                        ) : (
                          <PlayArrow />
                        )}
                      </IconButton>
                    )}
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => handleDeleteContainer(container.id)}
                      title="Delete"
                      disabled={actionLoading !== null || container.status === 'STOPPING' || container.status === 'RESTARTING'}
                    >
                      <Delete />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create Container Dialog */}
      <Dialog open={createDialog} onClose={() => setCreateDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create New Container</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Image Name"
                value={newContainer.image}
                onChange={(e) => setNewContainer({ ...newContainer, image: e.target.value })}
                helperText="e.g., ubuntu:latest, python:3.9, node:16"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                type="number"
                label="CPU Cores"
                value={newContainer.cpuCores}
                onChange={(e) => {
                  const cores = parseInt(e.target.value);
                  setNewContainer({ ...newContainer, cpuCores: cores });
                }}
                inputProps={{ min: 1, max: 4 }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                type="number"
                label="RAM (GB)"
                value={newContainer.ramBytes / (1024 * 1024 * 1024)}
                onChange={(e) => {
                  const gb = parseInt(e.target.value);
                  setNewContainer({ ...newContainer, ramBytes: gb * 1024 * 1024 * 1024 });
                }}
                inputProps={{ min: 1, max: 8 }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialog(false)}>Cancel</Button>
          <Button
            onClick={handleCreateContainer}
            variant="contained"
            disabled={creating}
          >
            {creating ? <CircularProgress size={24} /> : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </StudentLayout>
  );
}

export default Containers;
