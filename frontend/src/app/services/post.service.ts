import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface GeneratePostRequest {
  topic: string;
  targetAudience: string;
  tone: string;
  goal: string;
  length: string;
}

export interface PostHistory {
  id: number;
  title: string;
  content: string;
  tone: string;
  goal: string;
  targetAudience: string;
  createdAt: string;
  copyCount: number;
  published: boolean;
}

export interface SavedPost {
  id: number;
  content: string;
  tone: string;
  goal: string;
  targetAudience: string;
  createdAt: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class PostService {

  private apiUrl = '/api/posts';

  constructor(private http: HttpClient) {}

  // ✅ savePost returns SavedPost with id
  savePost(request: {
    content: string;
    tone: string;
    goal: string;
    targetAudience: string;
  }): Observable<SavedPost> {
    return this.http.post<SavedPost>(`${this.apiUrl}/save`, request);
  }

  getHistory(): Observable<PostHistory[]> {
    return this.http.get<PostHistory[]>(`${this.apiUrl}/history`);
  }

  markPublished(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/publish`, {});
  }

  schedulePost(id: number, scheduledAt: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/schedule`, { scheduledAt });
  }

  getScheduledPosts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/scheduled`);
  }

  cancelSchedule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/schedule`);
  }
}