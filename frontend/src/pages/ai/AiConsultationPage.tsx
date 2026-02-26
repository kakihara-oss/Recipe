import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useThreads, useThread, useMessages, useSendMessage } from '../../hooks/useAi'
import type { AiThreadResponse } from '../../types'

export default function AiConsultationPage() {
  const { threadId: threadIdParam } = useParams<{ threadId: string }>()
  const navigate = useNavigate()
  const [selectedThreadId, setSelectedThreadId] = useState<number>(0)
  const [sidebarOpen, setSidebarOpen] = useState(false)

  useEffect(() => {
    if (threadIdParam) {
      setSelectedThreadId(Number(threadIdParam))
    }
  }, [threadIdParam])

  const { data: threadsData } = useThreads({ page: 0, size: 50 })
  const { data: thread } = useThread(selectedThreadId)
  const { data: messages } = useMessages(selectedThreadId)
  const sendMutation = useSendMessage(selectedThreadId)

  const [input, setInput] = useState('')
  const chatEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = () => {
    if (!input.trim() || sendMutation.isPending) return
    sendMutation.mutate({ message: input.trim() })
    setInput('')
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  const selectThread = (t: AiThreadResponse) => {
    setSelectedThreadId(t.id)
    setSidebarOpen(false)
    navigate(`/ai/threads/${t.id}`, { replace: true })
  }

  return (
    <div className="flex h-[calc(100vh-8rem)] gap-0 overflow-hidden rounded-lg border border-gray-200 bg-white">
      {/* Mobile sidebar toggle */}
      <button
        onClick={() => setSidebarOpen(!sidebarOpen)}
        className="absolute left-2 top-2 z-10 rounded bg-white p-1 shadow lg:hidden"
      >
        <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>

      {/* Thread list sidebar */}
      <div className={`${sidebarOpen ? 'block' : 'hidden'} absolute inset-y-0 left-0 z-20 w-72 border-r border-gray-200 bg-white lg:static lg:block lg:w-72`}>
        <div className="flex h-12 items-center justify-between border-b border-gray-200 px-3">
          <span className="text-sm font-semibold text-gray-700">スレッド一覧</span>
          <Link to="/ai/new" className="rounded bg-blue-600 px-2 py-1 text-xs text-white hover:bg-blue-700">
            + 新規相談
          </Link>
        </div>
        <div className="overflow-y-auto" style={{ height: 'calc(100% - 3rem)' }}>
          {threadsData?.content.map((t) => (
            <button
              key={t.id}
              onClick={() => selectThread(t)}
              className={`w-full border-b border-gray-100 px-3 py-3 text-left transition-colors ${
                t.id === selectedThreadId ? 'bg-blue-50' : 'hover:bg-gray-50'
              }`}
            >
              <p className="truncate text-sm font-medium text-gray-800">{t.theme}</p>
              <p className="text-xs text-gray-400">
                {new Date(t.updatedAt).toLocaleDateString('ja-JP')}
              </p>
            </button>
          ))}
          {threadsData?.empty && (
            <p className="px-3 py-6 text-center text-sm text-gray-400">スレッドがありません</p>
          )}
        </div>
      </div>

      {/* Chat area */}
      <div className="flex flex-1 flex-col">
        {selectedThreadId > 0 && thread ? (
          <>
            {/* Chat header */}
            <div className="border-b border-gray-200 px-4 py-3">
              <p className="text-sm font-semibold text-gray-800">{thread.theme}</p>
              {thread.recipeName && (
                <p className="text-xs text-gray-500">関連レシピ: {thread.recipeName}</p>
              )}
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4">
              <div className="space-y-4">
                {messages?.map((msg) => (
                  <div
                    key={msg.id}
                    className={`flex ${msg.senderType === 'USER' ? 'justify-end' : 'justify-start'}`}
                  >
                    <div
                      className={`max-w-[75%] rounded-lg px-4 py-2 ${
                        msg.senderType === 'USER'
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-100 text-gray-800'
                      }`}
                    >
                      <p className="whitespace-pre-wrap text-sm">{msg.content}</p>
                      {msg.referencedArticles && msg.referencedArticles.length > 0 && (
                        <div className="mt-2 flex flex-wrap gap-1">
                          {msg.referencedArticles.map((article) => (
                            <Link
                              key={article.id}
                              to={`/knowledge/${article.id}`}
                              className="rounded bg-blue-50 px-2 py-0.5 text-xs text-blue-700 hover:bg-blue-100"
                            >
                              {article.title}
                            </Link>
                          ))}
                        </div>
                      )}
                      <p className={`mt-1 text-xs ${msg.senderType === 'USER' ? 'text-blue-200' : 'text-gray-400'}`}>
                        {new Date(msg.createdAt).toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' })}
                      </p>
                    </div>
                  </div>
                ))}
                <div ref={chatEndRef} />
              </div>
            </div>

            {/* Input */}
            <div className="border-t border-gray-200 p-3">
              <div className="flex gap-2">
                <textarea
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  onKeyDown={handleKeyDown}
                  placeholder="メッセージを入力... (Shift+Enter で改行)"
                  rows={2}
                  disabled={sendMutation.isPending}
                  className="flex-1 resize-none rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none disabled:bg-gray-100"
                />
                <button
                  onClick={handleSend}
                  disabled={!input.trim() || sendMutation.isPending}
                  className="self-end rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
                >
                  {sendMutation.isPending ? '...' : '送信'}
                </button>
              </div>
            </div>
          </>
        ) : (
          <div className="flex flex-1 items-center justify-center">
            <div className="text-center text-gray-400">
              <p className="mb-2">相談を選択するか、新規相談を始めてください</p>
              <Link to="/ai/new" className="text-blue-600 hover:underline">
                新規相談を始める
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
