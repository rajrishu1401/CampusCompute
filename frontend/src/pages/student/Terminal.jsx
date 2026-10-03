import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Typography, Paper, Button, Alert } from '@mui/material';
import { ArrowBack } from '@mui/icons-material';
import { Terminal as XTerm } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import 'xterm/css/xterm.css';
import StudentLayout from '../../components/StudentLayout';

function Terminal() {
  const { containerId } = useParams();
  const navigate = useNavigate();
  const terminalRef = useRef(null);
  const xtermRef = useRef(null);
  const wsRef = useRef(null);
  const [error, setError] = useState('');
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    // Initialize xterm.js
    const term = new XTerm({
      cursorBlink: true,
      fontSize: 14,
      theme: {
        background: '#1e1e1e',
        foreground: '#d4d4d4',
      },
    });

    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);

    if (terminalRef.current) {
      term.open(terminalRef.current);
      fitAddon.fit();
    }

    xtermRef.current = term;

    // Connect to WebSocket
    const token = localStorage.getItem('token');
    const wsUrl = `ws://localhost:8081/ws/terminal/${containerId}?token=${token}`;
    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      setConnected(true);
      term.writeln('Connected to container terminal.');
      term.writeln('');
    };

    ws.onmessage = (event) => {
      term.write(event.data);
    };

    ws.onerror = () => {
      setError('Failed to connect to terminal');
    };

    ws.onclose = () => {
      setConnected(false);
      term.writeln('');
      term.writeln('Connection closed.');
    };

    wsRef.current = ws;

    // Send input to WebSocket
    term.onData((data) => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send(data);
      }
    });

    // Handle window resize
    const handleResize = () => fitAddon.fit();
    window.addEventListener('resize', handleResize);

    // Cleanup
    return () => {
      window.removeEventListener('resize', handleResize);
      if (ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
      term.dispose();
    };
  }, [containerId]);

  return (
    <StudentLayout>
      <Box sx={{ mb: 2 }}>
        <Button
          startIcon={<ArrowBack />}
          onClick={() => navigate('/student/containers')}
        >
          Back to Containers
        </Button>
      </Box>

      <Typography variant="h4" gutterBottom fontWeight="bold">
        Container Terminal
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Container ID: {containerId}
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {connected && (
        <Alert severity="success" sx={{ mb: 2 }}>
          Terminal connected
        </Alert>
      )}

      <Paper
        elevation={3}
        sx={{
          p: 2,
          bgcolor: '#1e1e1e',
          minHeight: '500px',
          maxHeight: '70vh',
          overflow: 'auto',
        }}
      >
        <div ref={terminalRef} style={{ height: '100%' }} />
      </Paper>

      <Box sx={{ mt: 2 }}>
        <Typography variant="caption" color="text.secondary">
          Tip: Use Ctrl+C to interrupt running commands. Type 'exit' to close the session.
        </Typography>
      </Box>
    </StudentLayout>
  );
}

export default Terminal;
