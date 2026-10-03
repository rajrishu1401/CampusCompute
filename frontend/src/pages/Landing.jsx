import { useNavigate } from 'react-router-dom';
import { Container, Typography, Button, Box, Grid, Paper } from '@mui/material';
import { CloudQueue, School, Speed, Security } from '@mui/icons-material';

function Landing() {
  const navigate = useNavigate();

  const features = [
    {
      icon: <CloudQueue fontSize="large" color="primary" />,
      title: 'Resource Pooling',
      description: 'Efficient sharing of computing resources across campus labs',
    },
    {
      icon: <School fontSize="large" color="primary" />,
      title: 'Multi-Organization',
      description: 'Support for multiple colleges and universities on one platform',
    },
    {
      icon: <Speed fontSize="large" color="primary" />,
      title: 'Fast Deployment',
      description: 'Deploy containers in seconds with intelligent resource allocation',
    },
    {
      icon: <Security fontSize="large" color="primary" />,
      title: 'Secure & Isolated',
      description: 'Docker-based isolation ensures security and privacy',
    },
  ];

  return (
    <Box sx={{ bgcolor: 'background.default', minHeight: '100vh' }}>
      {/* Hero Section */}
      <Box
        sx={{
          bgcolor: 'primary.main',
          color: 'white',
          py: 8,
        }}
      >
        <Container maxWidth="lg">
          <Grid container spacing={4} alignItems="center">
            <Grid item xs={12} md={7}>
              <Typography variant="h2" component="h1" gutterBottom fontWeight="bold">
                CampusCompute
              </Typography>
              <Typography variant="h5" gutterBottom sx={{ mb: 4 }}>
                Campus-Aware Cloud Resource Pooling System
              </Typography>
              <Typography variant="body1" sx={{ mb: 4, opacity: 0.9 }}>
                Transform your idle lab computers into a powerful cloud platform. 
                Deploy containers, manage resources, and provide students with on-demand computing power.
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <Button
                  variant="contained"
                  size="large"
                  onClick={() => navigate('/register')}
                  sx={{ bgcolor: 'white', color: 'primary.main', '&:hover': { bgcolor: 'grey.100' } }}
                >
                  Register Organization
                </Button>
                <Button
                  variant="outlined"
                  size="large"
                  onClick={() => navigate('/login')}
                  sx={{ borderColor: 'white', color: 'white', '&:hover': { borderColor: 'grey.100' } }}
                >
                  Login
                </Button>
              </Box>
            </Grid>
            <Grid item xs={12} md={5}>
              <Paper
                elevation={3}
                sx={{
                  p: 3,
                  bgcolor: 'rgba(255, 255, 255, 0.1)',
                  backdropFilter: 'blur(10px)',
                  color: 'white',
                }}
              >
                <Typography variant="h6" gutterBottom>
                  Quick Stats
                </Typography>
                <Box sx={{ mt: 2 }}>
                  <Typography variant="body1">✓ Multi-tenant Architecture</Typography>
                  <Typography variant="body1">✓ Docker Container Management</Typography>
                  <Typography variant="body1">✓ Real-time Resource Monitoring</Typography>
                  <Typography variant="body1">✓ Adaptive Scheduling Algorithm</Typography>
                </Box>
              </Paper>
            </Grid>
          </Grid>
        </Container>
      </Box>

      {/* Features Section */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Typography variant="h3" align="center" gutterBottom fontWeight="bold">
          Key Features
        </Typography>
        <Typography variant="body1" align="center" color="text.secondary" sx={{ mb: 6 }}>
          Built for educational institutions to maximize resource utilization
        </Typography>
        <Grid container spacing={4}>
          {features.map((feature, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}>
              <Paper
                elevation={2}
                sx={{
                  p: 3,
                  height: '100%',
                  textAlign: 'center',
                  transition: 'transform 0.2s',
                  '&:hover': { transform: 'translateY(-4px)' },
                }}
              >
                <Box sx={{ mb: 2 }}>{feature.icon}</Box>
                <Typography variant="h6" gutterBottom>
                  {feature.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {feature.description}
                </Typography>
              </Paper>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* CTA Section */}
      <Box sx={{ bgcolor: 'grey.100', py: 6 }}>
        <Container maxWidth="md">
          <Typography variant="h4" align="center" gutterBottom fontWeight="bold">
            Ready to Get Started?
          </Typography>
          <Typography variant="body1" align="center" color="text.secondary" sx={{ mb: 3 }}>
            Register your organization and start utilizing campus resources efficiently
          </Typography>
          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2 }}>
            <Button
              variant="contained"
              size="large"
              onClick={() => navigate('/register')}
            >
              Register Now
            </Button>
            <Button
              variant="outlined"
              size="large"
              onClick={() => navigate('/login')}
            >
              Sign In
            </Button>
          </Box>
        </Container>
      </Box>

      {/* Footer */}
      <Box sx={{ bgcolor: 'grey.900', color: 'white', py: 3 }}>
        <Container maxWidth="lg">
          <Typography variant="body2" align="center">
            © 2026 CampusCompute - UPES Dehradun Major Project
          </Typography>
        </Container>
      </Box>
    </Box>
  );
}

export default Landing;
