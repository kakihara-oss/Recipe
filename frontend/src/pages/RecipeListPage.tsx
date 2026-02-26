import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import type { RecipeListItem, PageResponse } from '../api/client';
import api from '../api/client';

const STATUS_LABELS: Record<string, string> = {
  DRAFT: '下書き',
  PUBLISHED: '公開中',
  ARCHIVED: 'アーカイブ',
};

const STATUS_COLORS: Record<string, string> = {
  DRAFT: '#f59e0b',
  PUBLISHED: '#10b981',
  ARCHIVED: '#6b7280',
};

export default function RecipeListPage() {
  const [recipes, setRecipes] = useState<RecipeListItem[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRecipes();
  }, [page]);

  const fetchRecipes = async () => {
    setLoading(true);
    try {
      const res = await api.get<PageResponse<RecipeListItem>>('/recipes', {
        params: { page, size: 10 },
      });
      setRecipes(res.data.content);
      setTotalPages(res.data.totalPages);
    } catch (err) {
      console.error('Failed to fetch recipes', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">読み込み中...</div>;
  }

  return (
    <div>
      <div className="page-header">
        <h2>レシピ一覧</h2>
        <Link to="/recipes/new" className="btn btn-primary">
          + 新規レシピ作成
        </Link>
      </div>

      {recipes.length === 0 ? (
        <div className="empty-state">
          <p>レシピがまだありません</p>
          <Link to="/recipes/new" className="btn btn-primary">
            最初のレシピを作成する
          </Link>
        </div>
      ) : (
        <div className="recipe-grid">
          {recipes.map((recipe) => (
            <Link to={`/recipes/${recipe.id}`} key={recipe.id} className="recipe-card">
              <div className="recipe-card-header">
                <span
                  className="status-badge"
                  style={{ backgroundColor: STATUS_COLORS[recipe.status] || '#6b7280' }}
                >
                  {STATUS_LABELS[recipe.status] || recipe.status}
                </span>
                {recipe.category && (
                  <span className="category-badge">{recipe.category}</span>
                )}
              </div>
              <h3>{recipe.title}</h3>
              <p className="recipe-description">{recipe.description}</p>
              <div className="recipe-card-footer">
                <span>{recipe.createdByName}</span>
                <span>{recipe.servings && `${recipe.servings}人前`}</span>
              </div>
            </Link>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pagination">
          <button
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
            className="btn btn-secondary"
          >
            前へ
          </button>
          <span>{page + 1} / {totalPages}</span>
          <button
            disabled={page >= totalPages - 1}
            onClick={() => setPage(page + 1)}
            className="btn btn-secondary"
          >
            次へ
          </button>
        </div>
      )}
    </div>
  );
}
