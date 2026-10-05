# Diagrams Usage Guide for Presentation

## 📊 Quick Reference

### How to Open and Export

1. **Go to**: https://app.diagrams.net/
2. **Click**: File → Open from → Device
3. **Select**: Any `.drawio` file from `diagrams/` folder
4. **Export**: File → Export as → PNG (Scale: 200%)

---

## 🎯 When to Use Each Diagram

| Diagram | Timing | Purpose | Duration |
|---------|--------|---------|----------|
| **1. System Architecture** | 5 min | Show overall design | 2 min |
| **2. Container Lifecycle** | 15 min | Explain states after demo | 2 min |
| **3. Scheduling Algorithm** | 17 min | Technical deep-dive | 3 min |
| **4. Container Creation Flow** | 12 min | Explain demo sequence | 2 min |
| **5. Security Architecture** | Q&A | Answer security questions | 2 min |
| **6. Deployment Architecture** | Q&A | Answer scalability questions | 2 min |

---

## 📝 Presentation Flow with Diagrams

### Part 1: Introduction (0-5 min)
- Problem statement (slides/verbal)
- Solution overview (verbal)

### Part 2: Architecture (5-7 min)
**→ SHOW DIAGRAM 1: System Architecture**
- Explain 3 layers
- Show WebSocket communication
- Highlight lab computers

### Part 3: Live Demo (7-17 min)
- Admin workflow (3 min)
- Student workflow (5 min)
  - Create container
  - Access terminal
  - Stop container
  - Restart container
  
**→ SHOW DIAGRAM 4: Container Creation Flow** (12 min mark)
- Walk through the sequence
- Highlight timing (10-13 seconds)

**→ SHOW DIAGRAM 2: Container Lifecycle** (15 min mark)
- Explain AWS-style states
- Show intermediate states

### Part 4: Technical Details (17-22 min)

**→ SHOW DIAGRAM 3: Scheduling Algorithm**
- Explain scoring factors
- Walk through example calculation

### Part 5: Q&A (22-25 min)

**If asked about security:**
**→ SHOW DIAGRAM 5: Security Architecture**
- Multi-layer approach
- Enforcement points

**If asked about production/scalability:**
**→ SHOW DIAGRAM 6: Deployment Architecture**
- Infrastructure specs
- Cost analysis
- High availability setup

---

## 💡 Tips for Each Diagram

### Diagram 1: System Architecture
✅ **Do**:
- Point to each layer while explaining
- Trace the flow from user to container
- Emphasize real-time WebSocket

❌ **Don't**:
- Get lost in technical details
- Spend more than 2 minutes
- Read every label

**Key Points**:
1. "Three layers: Frontend (React), Backend (Spring Boot), Agents (Python)"
2. "Real-time bidirectional communication via WebSocket"
3. "Agents run on each lab computer, managing Docker"

---

### Diagram 2: Container Lifecycle
✅ **Do**:
- Highlight STOPPING and RESTARTING states
- Compare to AWS EC2
- Show complete circle (STOPPED → RESTARTING → RUNNING)

❌ **Don't**:
- Explain every single transition
- Use technical jargon
- Forget to mention agent confirmation

**Key Points**:
1. "Like AWS - shows actual state, not intended state"
2. "Intermediate states prevent confusion"
3. "Backend waits for agent confirmation"

---

### Diagram 3: Scheduling Algorithm
✅ **Do**:
- Walk through the flowchart step-by-step
- Use the example calculation box
- Explain why Device 2 wins

❌ **Don't**:
- Dive into code
- Rush through the scoring factors
- Forget to mention it's novel/custom

**Key Points**:
1. "Novel adaptive algorithm - not just random selection"
2. "Five factors: availability, load, reliability"
3. "Weighted scoring: Device 2 scores 0.655 vs 0.545"

---

### Diagram 4: Container Creation Flow
✅ **Do**:
- Follow the arrows sequentially
- Mention total time (10-13 seconds)
- Highlight WebSocket messages (red/green)

❌ **Don't**:
- Read every message
- Confuse the audience with too much detail
- Miss the timing indicators

**Key Points**:
1. "Sequence from student click to running container"
2. "Backend creates PENDING, scheduler selects device, agent confirms"
3. "Total time: about 10 seconds"

---

### Diagram 5: Security Architecture
✅ **Do**:
- Explain each layer briefly
- Show the enforcement flow at bottom
- Emphasize multiple checkpoints

❌ **Don't**:
- Go too deep into cryptography
- Spend more than 2 minutes
- Make it sound complicated

**Key Points**:
1. "Four security layers - defense in depth"
2. "Every request passes through authentication, authorization, quota check"
3. "Container isolation prevents inter-student access"

---

### Diagram 6: Deployment Architecture
✅ **Do**:
- Point out load balancing
- Highlight the cost savings (93%)
- Show 150 devices = 900+ cores

❌ **Don't**:
- Get into DevOps details
- Forget to mention high availability
- Miss the cost comparison

**Key Points**:
1. "Production-ready with load balancing and replication"
2. "150 lab computers = 900+ CPU cores, 1.5TB RAM"
3. "₹80K/year vs ₹12L/year for AWS - 93% savings"

---

## 🎤 Diagram Transitions (Scripts)

### Transitioning TO a Diagram

**Example 1**:
> "Let me show you the system architecture..." [Display Diagram 1]

**Example 2**:
> "To understand what just happened in the demo, here's the complete flow..." [Display Diagram 4]

**Example 3**:
> "Our scheduling algorithm is quite sophisticated. Let me walk you through it..." [Display Diagram 3]

### Transitioning FROM a Diagram

**Example 1**:
> "So that's the overall architecture. Now let me show you the actual system in action..." [Move to demo]

**Example 2**:
> "As you can see, the state machine ensures accuracy. Now let's talk about the algorithm..." [Move to next topic]

---

## 📸 Screenshot Instructions

If you can't open draw.io files during presentation:

1. Export all diagrams as PNG now
2. Save to `diagrams/exports/` folder:
   ```
   diagrams/
   ├── exports/
   │   ├── 1_system_architecture.png
   │   ├── 2_container_lifecycle.png
   │   ├── 3_scheduling_algorithm.png
   │   ├── 4_container_creation_flow.png
   │   ├── 5_security_architecture.png
   │   └── 6_deployment_architecture.png
   ```
3. Keep them open in tabs/windows
4. Alt+Tab to switch during presentation

---

## 🔧 Export Settings (Optimal Quality)

For each diagram:

1. File → Export as → PNG
2. Settings:
   - **Zoom**: 200%
   - **Border Width**: 10px
   - **Transparent Background**: No (use white)
   - **Shadow**: No
   - **Include**: Copy of diagram
3. Save with descriptive name
4. Verify quality by zooming in

---

## 🎯 Backup Plan

### If draw.io doesn't work:

**Plan A**: Use exported PNGs (recommended - do this before presentation)

**Plan B**: Use ASCII diagrams from PROJECT_DOCUMENTATION.md

**Plan C**: Verbal explanation with whiteboard/paper

---

## ✅ Pre-Presentation Checklist

**24 Hours Before**:
- [ ] Open each diagram in draw.io - verify they load
- [ ] Export all 6 diagrams as PNG (200% scale)
- [ ] Save PNGs in `diagrams/exports/` folder
- [ ] Test opening PNGs on presentation laptop
- [ ] Print key diagrams as backup (optional)

**1 Hour Before**:
- [ ] Open all PNG files in separate windows
- [ ] Arrange windows for easy Alt+Tab access
- [ ] Practice switching between diagrams
- [ ] Have backup USB with all files

**During Presentation**:
- [ ] Know which diagram comes when
- [ ] Don't read the diagram - explain it
- [ ] Use pointer/cursor to guide attention
- [ ] Keep each diagram under 3 minutes

---

## 🚀 Advanced: PowerPoint Integration (Optional)

If creating PowerPoint:

1. Export diagrams as PNG (200% scale)
2. Insert into slides:
   - Slide 3: Diagram 1 (Architecture)
   - Slide 8: Diagram 4 (Flow)
   - Slide 10: Diagram 2 (Lifecycle)
   - Slide 12: Diagram 3 (Algorithm)
   - Backup slides: Diagrams 5 & 6
3. Use "Appear" animation (not too fancy)
4. Keep one diagram per slide

---

## 📞 Emergency Contacts

If technical issues during presentation:
- Have PROJECT_DOCUMENTATION.md open (contains text explanations)
- Use whiteboard to draw simplified version
- Focus on verbal explanation, diagrams are support

---

**Remember**: Diagrams support your explanation, they don't replace it. You know the content - the diagrams just help visualize it!

**Good luck! 🎉**
