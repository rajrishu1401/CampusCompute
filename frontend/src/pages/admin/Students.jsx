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
    try {
      const students = csvData.split('\n').map(line => {
        const [studentId, email, fullName] = line.split(',').map(s => s.trim());
        return { studentId, email, fullName };
      }).filter(s => s.studentId && s.email);

      const response = await organizationService.uploadStudents({ students });
      if (response.success) {
        setUploadDialog(false);
        setCsvData('');
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
    </AdminLayout>
  );
}

export default Students;
