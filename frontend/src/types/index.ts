export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp?: number
}

export interface SSEEvent {
  type: 'text' | 'product' | 'error' | 'done'
  content: any
}

export interface Product {
  name: string
  platform: string
  price: string
  monthlySales?: string
  rating?: number
  competition?: string
  score?: number
  reason?: string
  imageUrl?: string
  url?: string
}
