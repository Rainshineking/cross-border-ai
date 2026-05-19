export interface ChatMessage {
  role: 'user' | 'assistant' | 'image'
  content: string
  timestamp?: number
  imageUrl?: string   // for image messages
  prompt?: string     // original prompt for image
}

export interface SSEEvent {
  type: 'text' | 'image' | 'product' | 'error' | 'done' | 'progress'
  content: any
  url?: string
  prompt?: string
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
