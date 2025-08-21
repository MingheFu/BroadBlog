import { ref } from 'vue'

export interface WebSocketMessage {
  type: string
  data: any
  timestamp: string
}

export interface NotificationMessage {
  id: number
  title: string
  content: string
  type: string
  recipientId: string
  createdAt: string
}

export interface CommentMessage {
  id: number
  content: string
  author: {
    id: number
    username: string
  }
  post: {
    id: number
    title: string
  }
  createdAt: string
}

class WebSocketService {
  private ws: WebSocket | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectInterval = 3000
  private url = (location.protocol === 'https:' ? 'wss' : 'ws') + '://' + location.host + '/ws'

  // Reactive state
  public isConnected = ref(false)
  public notifications = ref<NotificationMessage[]>([])
  public comments = ref<CommentMessage[]>([])

  // Event handlers
  public onNotification: ((notification: NotificationMessage) => void) | null = null
  public onComment: ((comment: CommentMessage) => void) | null = null
  public onConnect: (() => void) | null = null
  public onDisconnect: (() => void) | null = null

  connect() {
    try {
      this.ws = new WebSocket(this.url)
      
      this.ws.onopen = () => {
        console.log('WebSocket connected')
        this.isConnected.value = true
        this.reconnectAttempts = 0
        this.onConnect?.()
      }

      this.ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data)
          this.handleMessage(message)
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error)
        }
      }

      this.ws.onclose = () => {
        console.log('WebSocket disconnected')
        this.isConnected.value = false
        this.onDisconnect?.()
        this.attemptReconnect()
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error)
      }
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error)
    }
  }

  private handleMessage(message: WebSocketMessage) {
    switch (message.type) {
      case 'NOTIFICATION':
        const notification = message.data as NotificationMessage
        this.notifications.value.unshift(notification)
        this.onNotification?.(notification)
        break
      
      case 'COMMENT':
        const comment = message.data as CommentMessage
        this.comments.value.unshift(comment)
        this.onComment?.(comment)
        break
      
      case 'BROADCAST':
        const broadcastNotification = message.data as NotificationMessage
        this.notifications.value.unshift(broadcastNotification)
        this.onNotification?.(broadcastNotification)
        break
      
      default:
        console.log('Unknown message type:', message.type)
    }
  }

  private attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      
      setTimeout(() => {
        this.connect()
      }, this.reconnectInterval)
    } else {
      console.error('Max reconnection attempts reached')
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  send(message: WebSocketMessage) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.error('WebSocket is not connected')
    }
  }

  // Subscribe to specific topics
  subscribe(topic: string) {
    this.send({
      type: 'SUBSCRIBE',
      data: { topic },
      timestamp: new Date().toISOString()
    })
  }

  // Unsubscribe from specific topics
  unsubscribe(topic: string) {
    this.send({
      type: 'UNSUBSCRIBE',
      data: { topic },
      timestamp: new Date().toISOString()
    })
  }
}

// Create singleton instance
export const websocketService = new WebSocketService()
