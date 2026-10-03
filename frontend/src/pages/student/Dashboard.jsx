import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  Add,
  PlayArrow,
  Stop,
  Delete,
  Terminal as TerminalIcon,
} from '@mui/icons-material';
import StudentLayout from '../../components/StudentLayout';
import { containerService } from '../../services/container';

function Dashboard() {
  const navigate = useNavigate();
  const [containers, setContainers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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

  const runningContainers = containers.filter(c => c.status === 'RUNNING').length;

  return (
    <StudentLayout>
      <Typography variant="h4" gutterBottom fontWeight="bold">
        My Containers
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Manage your Docker containers
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* Summary Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="h3" color="primary" fontWeight="bold">
              {containers.length}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Total Containers
            </Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="h3" color="success.main" fontWeight="bold">
              {runningContainers}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Running Now
            </Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Button
              variant="contained"
              startIcon={<Add />}
              fullWidth
              size="large"
              onClick={() => navigate('/student/containers')}
            >
              Create New
            </Button>
          </Paper>
        </Grid>
      </Grid>

      {/* Containers Table */}
      <Paper>
        <Box sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">
            Your Containers
          </Typography>
        </Box>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell><strong>Image</strong></TableCell>
                  <TableCell><strong>Status</strong></TableCell>
                  <TableCell><strong>CPU</strong></TableCell>
                  <TableCell><strong>RAM</strong></TableCell>
                  <TableCell><strong>Created</strong></TableCell>
                  <TableCell><strong>Actions</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {containers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      <Box sx={{ py: 4 }}>
                        <Typography variant="body1" color="text.secondary" gutterBottom>
                          No containers yet
                        </Typography>
                        <Button
                          variant="contained"
                          startIcon={<Add />}
                          onClick={() => navigate('/student/containers')}
                          sx={{ mt: 2 }}
                        >
                          Create Your First Container
                        </Button>
                      </Box>
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
                      <TableCell>
                        {container.createdAt 
                          ? new Date(container.createdAt).toLocaleDateString()
                          : 'N/A'}
                      </TableCell>
                      <TableCell>
                        {container.status === 'RUNNING' && (
                          <>
                            <Button
                              size="small"
                              startIcon={<TerminalIcon />}
                              onClick={() => navigate(`/student/terminal/${container.id}`)}
                              sx={{ mr: 1 }}
                            >
                              Terminal
                            </Button>
                            <Button
                              size="small"
                              color="warning"
                              startIcon={<Stop />}
                              onClick={() => handleStopContainer(container.id)}
                              sx={{ mr: 1 }}
                            >
                              Stop
                            </Button>
                          </>
                        )}
                        <Button
                          size="small"
                          color="error"
                          startIcon={<Delete />}
                          onClick={() => handleDeleteContainer(container.id)}
                        >
                          Delete
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      {/* Getting Started Guide */}
      {containers.length === 0 && (
        <Paper sx={{ p: 3, mt: 3 }}>
          <Typography variant="h6" gutterBottom>
            Getting Started
          </Typography>
          <Typography variant="body2" color="text.secondary" paragraph>
            Create and manage Docker containers for your projects:
          </Typography>
          <Box component="ol" sx={{ pl: 2 }}>
            <Typography component="li" variant="body2" sx={{ mb: 1 }}>
              Click "Create New" or "Create Your First Container"
            </Typography>
            <Typography component="li" variant="body2" sx={{ mb: 1 }}>
              Choose a Docker image (ubuntu, python, node, etc.)
            </Typography>
            <Typography component="li" variant="body2" sx={{ mb: 1 }}>
              Allocate CPU cores and RAM for your container
            </Typography>
            <Typography component="li" variant="body2">
              Once running, access your container via the web terminal
            </Typography>
          </Box>
        </Paper>
      )}
    </StudentLayout>
  );
}

export default Dashboard;
