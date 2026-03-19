import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PostHistory, PostService, SavedPost } from './services/post.service';

@Component({
  selector: 'app-root',
  imports: [FormsModule, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
  encapsulation: ViewEncapsulation.None
})
export class App implements OnInit {

  topic = '';
  targetAudience = '';
  tone = 'Professional';
  goal = 'Engagement';
  length = 'Medium';

  generatedPost = '';
  isLoading = false;
  errorMessage = '';
  isStreaming = false;
  copySuccess = false;
  history: PostHistory[] = [];
  showHistory = false;

  // Scheduling state
  showScheduler = false;
  scheduledDate = '';
  scheduledTime = '';
  scheduledPosts: any[] = [];
  lastGeneratedPostId: number | null = null;
  today = new Date().toISOString().split('T')[0];

  // Render queue
  private renderQueue: string[] = [];
  private isRendering = false;

  // Typewriter state
  private twInterval: any = null;
  private twPhrases = [
    'Crafting your hook...',
    'Structuring the body...',
    'Almost ready...'
  ];
  private twPhraseIndex = 0;
  private twCharIndex = 0;
  private twDeleting = false;

  constructor(private postService: PostService) {}

  ngOnInit() {
    this.loadHistory();
    this.loadScheduledPosts();
  }

  // ── Generate Post ──
  generatePost() {
    if (!this.topic.trim()) {
      this.errorMessage = 'Please enter a topic.';
      return;
    }

    this.lastGeneratedPostId = null;
    this.isLoading = true;
    this.isStreaming = false;
    this.errorMessage = '';
    this.generatedPost = '';
    this.renderQueue = [];
    this.isRendering = false;

    setTimeout(() => this.startTypewriter(), 0);

    const request = {
      topic: this.topic,
      targetAudience: this.targetAudience,
      tone: this.tone,
      goal: this.goal,
      length: this.length
    };

    fetch('/api/posts/generate/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    }).then(response => {
      const reader = response.body!.getReader();
      const decoder = new TextDecoder();

      this.isLoading = false;
      this.isStreaming = true;
      this.stopTypewriter();

      let buffer = '';

      const readChunk = () => {
        reader.read().then(({ done, value }) => {

          if (done) {
            if (buffer.trim()) this.processLine(buffer);

            // ✅ Wait for render queue to empty then save
            const check = setInterval(() => {
              if (this.renderQueue.length === 0 && !this.isRendering) {
                this.isStreaming = false;
                clearInterval(check);

                console.log('Stream complete. Saving post to DB...');
                console.log('Content length:', this.generatedPost.length);

                this.postService.savePost({
                  content: this.generatedPost,
                  tone: this.tone,
                  goal: this.goal,
                  targetAudience: this.targetAudience
                }).subscribe({
                  next: (saved: SavedPost) => {
                    console.log('Post saved! ID:', saved.id);
                    this.lastGeneratedPostId = saved.id; // ✅ Schedule button appears
                    this.loadHistory();
                  },
                  error: (err: any) => {
                    console.error('Save failed:', err);
                    this.loadHistory();
                  }
                });
              }
            }, 100);
            return;
          }

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop() || '';
          lines.forEach(line => this.processLine(line));
          readChunk();

        }).catch(() => {
          this.stopTypewriter();
          this.isLoading = false;
          this.isStreaming = false;
        });
      };

      readChunk();

    }).catch(() => {
      this.stopTypewriter();
      this.errorMessage = 'Generation failed. Please try again.';
      this.isLoading = false;
      this.isStreaming = false;
    });
  }

  // ── Process SSE Lines ──
  processLine(line: string) {
    line = line.trim();
    if (line.startsWith('data:')) {
      let chunk = line.substring(5);
      chunk = chunk.replace(/<<N>>/g, '\n');

      const words = chunk.match(/\S+\s*|\n/g) || [];
      words.forEach(word => this.renderQueue.push(word));

      if (!this.isRendering) {
        this.renderNextWord();
      }
    }
  }

  // ── Render Words ──
  renderNextWord() {
    if (this.renderQueue.length === 0) {
      this.isRendering = false;
      return;
    }

    this.isRendering = true;
    const word = this.renderQueue.shift()!;
    this.generatedPost += word;

    setTimeout(() => this.renderNextWord(), 30);
  }

  // ── Typewriter ──
  startTypewriter() {
    this.twPhraseIndex = 0;
    this.twCharIndex = 0;
    this.twDeleting = false;
    this.runTypewriter();
  }

  runTypewriter() {
    this.twInterval = setTimeout(() => {
      const el = document.getElementById('tw-text');
      if (!el) return;

      const phrase = this.twPhrases[this.twPhraseIndex];

      if (!this.twDeleting) {
        this.twCharIndex++;
        el.textContent = phrase.slice(0, this.twCharIndex);

        if (this.twCharIndex === phrase.length) {
          this.twDeleting = true;
          this.twInterval = setTimeout(() => this.runTypewriter(), 1200);
          return;
        }
      } else {
        this.twCharIndex--;
        el.textContent = phrase.slice(0, this.twCharIndex);

        if (this.twCharIndex === 0) {
          this.twDeleting = false;
          this.twPhraseIndex = (this.twPhraseIndex + 1) % this.twPhrases.length;
        }
      }

      this.runTypewriter();
    }, this.twDeleting ? 40 : 65);
  }

  stopTypewriter() {
    if (this.twInterval) {
      clearTimeout(this.twInterval);
      this.twInterval = null;
    }
    const el = document.getElementById('tw-text');
    if (el) el.textContent = '';
  }

  // ── Scheduling ──
  openScheduler() {
    const oneHourLater = new Date(Date.now() + 60 * 60 * 1000);
    this.scheduledDate = oneHourLater.toISOString().split('T')[0];
    this.scheduledTime = oneHourLater.toTimeString().slice(0, 5);
    this.showScheduler = true;
  }

  schedulePost() {
    if (!this.lastGeneratedPostId) return;

    const scheduledAt = `${this.scheduledDate}T${this.scheduledTime}:00`;

    this.postService.schedulePost(this.lastGeneratedPostId, scheduledAt)
      .subscribe({
        next: () => {
          this.showScheduler = false;
          this.loadScheduledPosts();
          alert(`Post scheduled for ${this.scheduledDate} at ${this.scheduledTime} ✅`);
        },
        error: () => alert('Failed to schedule post.')
      });
  }

  loadScheduledPosts() {
    this.postService.getScheduledPosts().subscribe({
      next: (posts) => this.scheduledPosts = posts,
      error: () => {}
    });
  }

  cancelSchedule(id: number) {
    this.postService.cancelSchedule(id).subscribe({
      next: () => this.loadScheduledPosts()
    });
  }

  // ── Clipboard ──
  copyToClipboard() {
    navigator.clipboard.writeText(this.generatedPost).then(() => {
      this.copySuccess = true;
      setTimeout(() => this.copySuccess = false, 2500);
    });
  }

  // ── History ──
  loadHistory() {
    this.postService.getHistory().subscribe({
      next: (posts) => this.history = posts,
      error: () => {}
    });
  }
}