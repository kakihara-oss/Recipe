import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import type { RecipeDetail } from '../api/client';
import api from '../api/client';

const STATUS_LABELS: Record<string, string> = {
  DRAFT: '下書き',
  PUBLISHED: '公開中',
  ARCHIVED: 'アーカイブ',
};

export default function RecipeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [recipe, setRecipe] = useState<RecipeDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRecipe();
  }, [id]);

  const fetchRecipe = async () => {
    try {
      const res = await api.get<RecipeDetail>(`/recipes/${id}`);
      setRecipe(res.data);
    } catch {
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (newStatus: string) => {
    try {
      await api.put(`/recipes/${id}/status`, { status: newStatus });
      fetchRecipe();
    } catch (err) {
      console.error('Status update failed', err);
    }
  };

  const handleDelete = async () => {
    if (!confirm('このレシピを削除しますか？')) return;
    try {
      await api.delete(`/recipes/${id}`);
      navigate('/');
    } catch (err) {
      console.error('Delete failed', err);
    }
  };

  if (loading) return <div className="loading">読み込み中...</div>;
  if (!recipe) return null;

  return (
    <div className="recipe-detail">
      <div className="detail-header">
        <Link to="/" className="back-link">← 一覧に戻る</Link>
        <div className="detail-actions">
          {recipe.status === 'DRAFT' && (
            <button onClick={() => handleStatusChange('PUBLISHED')} className="btn btn-success">
              公開する
            </button>
          )}
          {recipe.status === 'PUBLISHED' && (
            <button onClick={() => handleStatusChange('ARCHIVED')} className="btn btn-secondary">
              アーカイブ
            </button>
          )}
          {recipe.status === 'ARCHIVED' && (
            <button onClick={() => handleStatusChange('PUBLISHED')} className="btn btn-success">
              再公開する
            </button>
          )}
          <button onClick={handleDelete} className="btn btn-danger">削除</button>
        </div>
      </div>

      <div className="detail-hero">
        <span className="status-badge-large">
          {STATUS_LABELS[recipe.status] || recipe.status}
        </span>
        {recipe.category && <span className="category-badge-large">{recipe.category}</span>}
        <h1>{recipe.title}</h1>
        <p className="detail-meta">
          作成者: {recipe.createdBy.name}（{recipe.createdBy.role}）
          ・ {recipe.servings && `${recipe.servings}人前`}
        </p>
      </div>

      {recipe.description && (
        <section className="detail-section">
          <h2>説明</h2>
          <p>{recipe.description}</p>
        </section>
      )}

      {recipe.concept && (
        <section className="detail-section">
          <h2>コンセプト</h2>
          <p>{recipe.concept}</p>
        </section>
      )}

      {recipe.story && (
        <section className="detail-section">
          <h2>ストーリー</h2>
          <p>{recipe.story}</p>
        </section>
      )}

      {recipe.ingredients && recipe.ingredients.length > 0 && (
        <section className="detail-section">
          <h2>食材</h2>
          <table className="detail-table">
            <thead>
              <tr>
                <th>食材名</th>
                <th>数量</th>
                <th>単位</th>
                <th>下処理メモ</th>
              </tr>
            </thead>
            <tbody>
              {recipe.ingredients.map((ing) => (
                <tr key={ing.id}>
                  <td>{ing.ingredientName}</td>
                  <td>{ing.quantity}</td>
                  <td>{ing.unit}</td>
                  <td>{ing.preparationNote || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {recipe.cookingSteps && recipe.cookingSteps.length > 0 && (
        <section className="detail-section">
          <h2>調理手順</h2>
          <div className="steps-list">
            {recipe.cookingSteps
              .sort((a, b) => a.stepNumber - b.stepNumber)
              .map((step) => (
                <div key={step.id} className="step-item">
                  <div className="step-number">{step.stepNumber}</div>
                  <div className="step-content">
                    <p className="step-description">{step.description}</p>
                    <div className="step-meta">
                      {step.durationMinutes && <span>⏱ {step.durationMinutes}分</span>}
                      {step.temperature && <span>🌡 {step.temperature}</span>}
                    </div>
                    {step.tips && <p className="step-tips">💡 {step.tips}</p>}
                  </div>
                </div>
              ))}
          </div>
        </section>
      )}

      {recipe.serviceDesign && (
        <section className="detail-section">
          <h2>サービス設計</h2>
          <div className="design-grid">
            {recipe.serviceDesign.platingInstructions && (
              <div className="design-item">
                <h4>盛り付け</h4>
                <p>{recipe.serviceDesign.platingInstructions}</p>
              </div>
            )}
            {recipe.serviceDesign.serviceMethod && (
              <div className="design-item">
                <h4>提供方法</h4>
                <p>{recipe.serviceDesign.serviceMethod}</p>
              </div>
            )}
            {recipe.serviceDesign.customerScript && (
              <div className="design-item">
                <h4>お客様への声かけ</h4>
                <p>{recipe.serviceDesign.customerScript}</p>
              </div>
            )}
            {recipe.serviceDesign.storytelling && (
              <div className="design-item">
                <h4>ストーリーテリング</h4>
                <p>{recipe.serviceDesign.storytelling}</p>
              </div>
            )}
          </div>
        </section>
      )}

      {recipe.experienceDesign && (
        <section className="detail-section">
          <h2>体験設計</h2>
          <div className="design-grid">
            {recipe.experienceDesign.targetScene && (
              <div className="design-item">
                <h4>ターゲットシーン</h4>
                <p>{recipe.experienceDesign.targetScene}</p>
              </div>
            )}
            {recipe.experienceDesign.emotionalKeyPoints && (
              <div className="design-item">
                <h4>感動ポイント</h4>
                <p>{recipe.experienceDesign.emotionalKeyPoints}</p>
              </div>
            )}
            {recipe.experienceDesign.seasonalPresentation && (
              <div className="design-item">
                <h4>季節の演出</h4>
                <p>{recipe.experienceDesign.seasonalPresentation}</p>
              </div>
            )}
            {recipe.experienceDesign.sensoryAppeal && (
              <div className="design-item">
                <h4>五感への訴求</h4>
                <p>{recipe.experienceDesign.sensoryAppeal}</p>
              </div>
            )}
          </div>
        </section>
      )}
    </div>
  );
}
