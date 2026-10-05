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
import { Upload } from '@mui/icons-material';
import AdminLayout from '../../components/AdminLayout';
import { organizationService } from '../../services/organization';

function Students() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [uploadDialog, setUploadDialog] = useState(false);
  const [csvData, setCsvData] = useState('');
  const [uploading, setUploading] = useState(false);
  const [passwordDialog, setPasswordDialog] = useState(false);
  const [createdStudentsWithPasswords, setCreatedStudentsWithPasswords] = useState([]);

  useEffect(() => {
    fetchStudents();
  }, []);

  const fetchStudents = async () => {
    try {
      const response = await organizationService.getStudents();
      if (response.success) {
        setStudents(response.data);
      } else {
        setError('Failed to load students');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load students');
    } finally {
      setLoading(false);
    }
  };

  const handleUploadStudents = async () => {
    setUploading(true);
    setError('');
    try {
      const students = csvData.split('\n').map(line => {
        const [studentId, email, fullName] = line.split(',').map(s => s.trim());
        return { studentId, email, fullName };
      }).filter(s => s.studentId && s.email);

      const response = await organizationService.uploadStudents({ students });
      
      if (response.success) {
        setUploadDialog(false);
        setCsvData('');
        
        // Show passwords dialog
        const studentsData = response.data?.students || [];
        setCreatedStudentsWithPasswords(studentsData);
        setPasswordDialog(true);
        
        // Refresh students list
        fetchStudents();
      } else {
        setError(response.message || 'Failed to upload students');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload students');
    } finally {
      setUploading(false);
    }
  };

  const downloadPasswordsCSV = () => {
    const csvContent = [
      'Student ID,Username,Email,Full Name,Temporary Password',
      ...createdStudentsWithPasswords.map(s => 
        `${s.studentId},${s.username},${s.email},${s.fullName},${s.temporaryPassword}`
      )
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `student-passwords-${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
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
            Students
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage student accounts and usage
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Upload />}
          onClick={() => setUploadDialog(true)}
        >
          Upload Students
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Student ID</strong></TableCell>
              <TableCell><strong>Name</strong></TableCell>
              <TableCell><strong>Email</strong></TableCell>
              <TableCell><strong>Status</strong></TableCell>
              <TableCell><strong>Containers</strong></TableCell>
              <TableCell><strong>CPU Used</strong></TableCell>
              <TableCell><strong>RAM Used</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {students.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography variant="body2" color="text.secondary">
                    No students found. Click "Upload Students" to add students.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              students.map((student) => (
                <TableRow key={student.id}>
                  <TableCell>{student.studentId}</TableCell>
                  <TableCell>{student.fullName}</TableCell>
                  <TableCell>{student.email}</TableCell>
                  <TableCell>
                    <Chip
                      label={student.approved ? 'Approved' : 'Pending'}
                      color={student.approved ? 'success' : 'warning'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {student.runningContainers}/{student.quotaMaxContainers}
                  </TableCell>
                  <TableCell>
                    {student.totalCpuUsed}/{student.quotaMaxCpu}
                  </TableCell>
                  <TableCell>
                    {student.totalRamUsed}/{student.quotaMaxRam} GB
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Upload Students Dialog */}
      <Dialog open={uploadDialog} onClose={() => setUploadDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Upload Students</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Enter student data in CSV format (one student per line):
            <br />
            <code>student_id, email, full_name</code>
          </Typography>
          <TextField
            fullWidth
            multiline
            rows={8}
            value={csvData}
            onChange={(e) => setCsvData(e.target.value)}
            placeholder="500101234, john@example.com, John Doe&#10;500101235, jane@example.com, Jane Smith"
            helperText="Format: student_id, email, full_name (one per line)"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setUploadDialog(false)}>Cancel</Button>
          <Button
            onClick={handleUploadStudents}
            variant="contained"
            disabled={uploading || !csvData.trim()}
          >
            {uploading ? <CircularProgress size={24} /> : 'Upload'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Password Display Dialog */}
      <Dialog 
        open={passwordDialog} 
        onClose={() => setPasswordDialog(false)} 
        maxWidth="md" 
        fullWidth
        disableEscapeKeyDown
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="h6" component="span">
              Students Created Successfully!
            </Typography>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 3 }}>
            <Typography variant="subtitle2" gutterBottom>
              ⚠️ Save These Passwords Now!
            </Typography>
            <Typography variant="body2">
              These temporary passwords will only be shown once. Please save them and distribute to students.
              Students will use their Student ID as username.
            </Typography>
          </Alert>

          {createdStudentsWithPasswords.length > 0 ? (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.50' }}>
                    <TableCell><strong>Student ID</strong></TableCell>
                    <TableCell><strong>Email</strong></TableCell>
                    <TableCell><strong>Full Name</strong></TableCell>
                    <TableCell><strong>Temporary Password</strong></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {createdStudentsWithPasswords.map((student) => (
                    <TableRow key={student.studentId} hover>
                      <TableCell>
                        <Typography variant="body2" fontFamily="monospace">
                          {student.studentId}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {student.email}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {student.fullName}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <code style={{
                            background: '#f5f5f5',
                            padding: '4px 8px',
                            borderRadius: '4px',
                            fontWeight: 'bold',
                            color: '#d32f2f'
                          }}>
                            {student.temporaryPassword}
                          </code>
                          <Button
                            size="small"
                            onClick={() => copyToClipboard(student.temporaryPassword)}
                            sx={{ minWidth: 'auto', p: 0.5 }}
                          >
                            📋
                          </Button>
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Alert severity="error" sx={{ mb: 2 }}>
              <Typography variant="body2">
                No password data was returned from the server. This may indicate a backend issue.
                Please try again or contact support.
              </Typography>
            </Alert>
          )}

          <Box sx={{ mt: 3, p: 2, bgcolor: 'info.lighter', borderRadius: 1, border: '1px solid', borderColor: 'info.light' }}>
            <Typography variant="subtitle2" color="info.dark" gutterBottom>
              📝 How Students Login:
            </Typography>
            <Typography variant="body2" color="text.secondary">
              • <strong>Username:</strong> Their Student ID (e.g., 500101234)
              <br />
              • <strong>Password:</strong> The temporary password shown above
              <br />
              • Students should be asked to change their password on first login
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button 
            onClick={downloadPasswordsCSV}
            variant="outlined"
            startIcon={<Upload />}
          >
            Download CSV
          </Button>
          <Button 
            onClick={() => setPasswordDialog(false)} 
            variant="contained"
          >
            I've Saved the Passwords
          </Button>
        </DialogActions>
      </Dialog>
    </AdminLayout>
  );
}

export default Students;
