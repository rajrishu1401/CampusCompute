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
  const [createDialog, setCreateDialog] = useState(false);
  const [creating, setCreating] = useState(false);
  const [newContainer, setNewContainer] = useState({
    imageName: 'ubuntu:latest',
    cpuCores: 1,
    ramGb: 1,
  });

  useEffect(() => {
    fetchContainers();
  }, []);

  const fetchContainers = async () => {
    try {
      const response = await containerService.getContainers();
      if (response.success) {
        setContainers(response.data);
      } else {
        setError('Failed to load containers');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load containers');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateContainer = async () => {
    setCreating(true);
    try {
      const response = await containerService.createContainer(newContainer);
      if (response.success) {
        setCreateDialog(false);
        setNewContainer({ imageName: 'ubuntu:latest', cpuCores: 1, ramGb: 1 });
        fetchContainers();
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
    try {
      await containerService.stopContainer(id);
      fetchContainers();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to stop container');
    }
  };

  const handleDeleteContainer = async (id) => {
    if (window.confirm('Are you sure you want to delete this container?')) {
      try {
        await containerService.deleteContainer(id);
        fetchContainers();
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to delete container');
      }
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      RUNNING: 'success',
      STOPPED: 'default',
      PENDING: 'warning',
      FAILED: 'error',
    };
    return colors[status] || 'default';
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
                  <TableCell>{container.imageName}</TableCell>
                  <TableCell>
                    <Chip
                      label={container.status}
                      color={getStatusColor(container.status)}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{container.cpuCores} cores</TableCell>
                  <TableCell>{container.ramGb} GB</TableCell>
                  <TableCell>{container.deviceId || 'N/A'}</TableCell>
                  <TableCell>
                    {container.status === 'RUNNING' && (
                      <>
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => navigate(`/student/terminal/${container.id}`)}
                          title="Open Terminal"
                        >
                          <Terminal />
                        </IconButton>
                        <IconButton
                          size="small"
                          color="warning"
                          onClick={() => handleStopContainer(container.id)}
                          title="Stop"
                        >
                          <Stop />
                        </IconButton>
                      </>
                    )}
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => handleDeleteContainer(container.id)}
                      title="Delete"
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
                value={newContainer.imageName}
                onChange={(e) => setNewContainer({ ...newContainer, imageName: e.target.value })}
                helperText="e.g., ubuntu:latest, python:3.9, node:16"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                type="number"
                label="CPU Cores"
                value={newContainer.cpuCores}
                onChange={(e) => setNewContainer({ ...newContainer, cpuCores: parseInt(e.target.value) })}
                inputProps={{ min: 1, max: 4 }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                type="number"
                label="RAM (GB)"
                value={newContainer.ramGb}
                onChange={(e) => setNewContainer({ ...newContainer, ramGb: parseInt(e.target.value) })}
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
